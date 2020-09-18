#!/usr/bin/env bash

export PS4='+${BASH_SOURCE}:${LINENO}:${FUNCNAME[0]:+${FUNCNAME[0]}():} '

repo_dir="$(git rev-parse --show-toplevel)"
aac_dir=/tmp/aac/demo-folder

set -e
set -u
set -o pipefail

function print-usage() {
    echo "Usage: $0 [-C|--cleanup][-h|--help]"
}

function print-help() {
    cat <<EOH
This script will:
 - run ./gradlew build (not clean)
 - create a demo folder where it's easy to execute the binary
 - that folder will be as if 'init' and 'au init' had already been run

Options:
   -h, --help       Print this help and exit
EOH
}

function maybe-create-init-au-yaml() {
    # ASSUMES running from within the create demo folder, not the repo dir
    for br in $(git for-each-ref --format='%(refname:short)'); do
        case $br in
        test) return ;; # If there's already a branch, don't change it
        esac
    done

    run git checkout -q -b test
    mkdir -p architecture-update/test
    cp "$repo_dir/documentation/products/arch-as-code/architecture-updates/show-tdd-in-diff/architecture-update.yml" architecture-update/test
    git add .
    git commit -q -m 'Set up demonstration AU'
    # TODO: The script leaves you in 'test' branch if exists, but in 'master'
    #       if copying in demo files
    run git checkout -q master
}

# shellcheck disable=SC1090
. "${0%/*}/colors.sh"
export TTY=false
[[ -t 1 ]] && TTY=true

# A trick to show output on failure, but not on success
outfile="/tmp/out"

# Note: STDOUT and STDERR may be mixed.  This function does not attempt to
# address this: STDERR will always appear before STDOUT using this function
function run() {
    "$@" >"$outfile" || {
        local rc=$?
        cat "$outfile"
        return $rc
    }
}

# shellcheck disable=SC2214
while getopts :h-: opt; do
    [[ $opt == - ]] && opt=${OPTARG%%=*} OPTARG=${OPTARG#*=}
    case $opt in
    h | help)
        print-help
        exit 0
        ;;
    *)
        print-usage
        exit 2
        ;;
    esac
done
shift $((OPTIND - 1))

# ASSUMES script is run from somewhere within the project repo
repo_dir="$(git rev-parse --show-toplevel)"
cd "$repo_dir"

echo "Working..."

mkdir -p "$aac_dir"

if [[ -r "$aac_dir"/.arch-as-code ]]; then
    :  # Keep existing
elif [[ -r ~/.arch-as-code ]]; then
    cp -a ~/.arch-as-code "$aac_dir"
else
    echo "$0: No $HOME/.arch-as-code configuration files" >&2
    exit 1
fi

rm -rf $aac_dir/.install
mkdir -p $aac_dir/.install

cp ./scripts/demo-git-ignore $aac_dir/.gitignore
cp ./.java-version $aac_dir
mkdir -p $aac_dir/.install/bin

run ./gradlew bootJar
cp ./build/libs/arch-as-code-*.jar $aac_dir/.install/bin

cat <<EOS >$aac_dir/.install/bin/arch-as-code
#!/bin/sh

# The extra flag is to quiet WARNINGS from Jackson
exec java --illegal-access=permit -jar "$aac_dir"/.install/bin/arch-as-code-*.jar "\$@"
EOS
chmod a+rx $aac_dir/.install/bin/arch-as-code

cd $aac_dir

# shellcheck disable=SC2016
ln -fs .install/bin/arch-as-code .

# This file is optional
# TODO: Stop replacing user-edited files
[[ -r product-architecture.yml ]] && {
    mv product-architecture.yml product-architecture.yml.bak || {
        echo "$0: WARNING: No locally edited product-architecture.yml; ignoring" >&2
    }
}

# TODO: Check if:
# 1) Credentials aready exist, and use them
# 2) If NOT, prompt user for the 3 needed values
run .install/bin/arch-as-code init -i i -k i -s s .
run .install/bin/arch-as-code au init -c c -p p -s s .

# TODO: Do not destroy the existing file
[[ -r product-architecture.yml.bak ]] && mv product-architecture.yml.bak product-architecture.yml

if [[ ! -d .git ]]; then
    run git init
    run git add .
    run git commit -m Init
fi

maybe-create-init-au-yaml

cat <<EOM
Demo folder created in '$PWD'.
Change to that directory, and use ./arch-as-code or "aac" alias.
(Once there, you may find 'alias aac=\$PWD/arch-as-code' helpful.)
This is setup as a Git repo (or there was already one present).
If there were already a '$PWD' directory, we overwrite the AaC
parts only.
If there were already a 'test' branch for architecture updates, we left it be.
If there were already a '.arch-as-code/' directory, we left it be, else we
copied one from your home directory.
EOM
