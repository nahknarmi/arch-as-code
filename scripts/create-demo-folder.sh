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

# find and go to repo root dir
# TODO: Ask git directly: `git rev-parse --show-toplevel`
d="$(dirname "${BASH_SOURCE[0]}")"
dir="$(cd "$(dirname "$d")" && pwd)/$(basename "$d")"
cd "$dir"
cd ..

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

run git init

pwd # Tell the user where to find the demo folder

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

# copy .arch-as-code from repo root
rm -rf .arch-as-code
cp -r "$dir"/../.arch-as-code .

# add executable to folder
# shellcheck disable=SC2016
ln -fs .install/bin/arch-as-code .

cat <<EOM




Demo folder created. To cd there, run:
   cd $(pwd)
Run ./arch-as-code
EOM
