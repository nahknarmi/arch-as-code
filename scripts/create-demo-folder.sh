#!/usr/bin/env bash

export PS4='+${BASH_SOURCE}:${LINENO}:${FUNCNAME[0]:+${FUNCNAME[0]}():} '

set -e
set -u
set -o pipefail

function print-usage() {
    echo "Usage: $0 [-C|--cleanup][-h|--help]"
}

function print-help() {
    cat <<EOH
This script will:
 - run ./gradlew clean
 - run ./gradlew distZip
 - create a demo folder where it's easy to execute the binary
 - that folder will be as if 'init' and 'au init' had already been run

Options:
   -h, --help       Print this help and exit
EOH
}

function maybe-create-init-au-yaml() {
    # ASSUMES running from within the create demo folder, not the repo dir
    for br in $(git for-each-ref --shell --format='%(refname:short)'); do
        case $br in
        test) return ;; # If there's already a branch, don't change it
        esac
    done

    run git checkout -b test
    mkdir -p architecture-update/test
    cp "$repo_dir/documentation/products/arch-as-code/architecture-updates/show-tdd-in-diff/architecture-update.yml" architecture-update/test
    git add .
    git commit -m 'Set up demonstration AU'
    run git checkout master
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

rm -rf /tmp/aac/demo-folder/.arch-as-code
rm -rf /tmp/aac/demo-folder/.install
mkdir -p /tmp/aac/demo-folder/.install

run ./gradlew clean # Start clean

cp ./scripts/demo-git-ignore /tmp/aac/demo-folder/.gitignore
cp ./.java-version /tmp/aac/demo-folder

run ./gradlew bootJar
mkdir -p /tmp/aac/demo-folder/.install/bin
cp ./build/libs/arch-as-code-*.jar /tmp/aac/demo-folder/.install/bin

cat <<EOS >/tmp/aac/demo-folder/.install/bin/arch-as-code
#!/bin/sh

# The extra flag is to quiet WARNINGS from Jackson
exec java --illegal-access=permit -jar /tmp/aac/demo-folder/.install/bin/arch-as-code-*.jar "\$@"
EOS
chmod a+rx /tmp/aac/demo-folder/.install/bin/arch-as-code

cd /tmp/aac/demo-folder

# This file is optional
[[ -r product-architecture.yml ]] && {
    mv product-architecture.yml product-architecture.yml.bak || {
        echo "$0: WARNING: No locally edited product-architecture.yml; ignoring" >&2
    }
}

run .install/bin/arch-as-code init -i i -k i -s s .
run .install/bin/arch-as-code au init -c c -p p -s s .

# Optionally restore the backup, if exists
[[ -r product-architecture.yml.bak ]] && mv product-architecture.yml.bak product-architecture.yml

# Create initial AaC settings files if none already present
if [[ -d ~/.arch-as-code ]]; then # Home dir first
    cp -r ~/.arch-as-code .
else # Project repo files as a fallback
    cp -r "$repo_dir"/.arch-as-code .
fi

# shellcheck disable=SC2016
ln -fs .install/bin/arch-as-code .

if [[ ! -d .git ]]; then
    run git init
    run git add .
    run git commit -m Init
fi

maybe-create-init-au-yaml

cat <<EOM
Demo folder created in $PWD.
Change to that directory, and use ./arch-as-code or "aac" alias.
(Once there, you may find 'alias aac=\$PWD/arch-as-code' helpful)
This is setup as a Git repo (or there was already one present).
EOM
