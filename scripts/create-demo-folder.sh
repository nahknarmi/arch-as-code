#!/usr/bin/env bash

export PS4='+${BASH_SOURCE}:${LINENO}:${FUNCNAME[0]:+${FUNCNAME[0]}():} '

set -e
set -u
set -o pipefail

function print-usage() {
    echo "Usage: $0 [-h|--help]"
}

function print-help() {
    cat <<EOH
This script will:
 - run ./gradlew clean
 - run ./gradlew distZip
 - create a demo folder where it's easy to execute the binary
 - that folder will be as if 'init' and 'au init' had already been run
EOH
}

# shellcheck disable=SC1090
. "${0%/*}/colors.sh"
export TTY=false
[[ -t 1 ]] && TTY=true

tmpdir="${TMPDIR-/tmp}/aac-$$"
# Avoid leaving behind junk on disk unless a command fails
trap '[[ 0 == $? ]] && rm -rf "$tmpdir"' EXIT
# A trick to show output on failure, but not on success
outfile="$tmpdir/out"

# Note: STDOUT and STDERR may be mixed.  This function does not attempt to
# address this: STDERR will always appear before STDOUT using this function
function run() {
    "$@" >"$outfile" || {
        rc=$?
        cat "$outfile"
        exit $rc
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
d="$(dirname "${BASH_SOURCE[0]}")"
dir="$(cd "$(dirname "$d")" && pwd)/$(basename "$d")"
cd "$dir"
cd ..

rm -rf "$tmpdir"/demo-folder/.arch-as-code
rm -rf "$tmpdir"/demo-folder/.install
mkdir -p "$tmpdir"/demo-folder/.install

# remove existing
run ./gradlew clean
rm -rf build

cp ./scripts/demo-git-ignore $tmpdir/demo-folder/.gitignore

# build
run ./gradlew distZip
cd build/distributions
run unzip *.zip
rm *.zip
cd *
mv ./* $tmpdir/demo-folder/.install/

cd "$tmpdir"/demo-folder

pwd
run git init

# This file is optional; created locally
mv product-architecture.yml product-architecture.yml.bak || {
    echo "$0: WARNING: No locally edited product-architecture.yml: continuing" >&2
}

run .install/bin/arch-as-code init -i i -k i -s s .
run .install/bin/arch-as-code au init -c c -p p -s s .

# Optionally restore the backup
[[ -r product-architecture.yml.bak ]] && mv product-architecture.yml.bak product-architecture.yml || true

# copy .arch-as-code from repo root
rm -rf .arch-as-code
cp -r $dir/../.arch-as-code .

# add executable to folder
# shellcheck disable=SC2016
echo 'd="$(dirname "${BASH_SOURCE[0]}")"; dir="$(cd "$(dirname "$d")" && pwd)/$(basename "$d")"; "${dir}"/.install/bin/arch-as-code "$@";' >arch-as-code.sh
chmod +x arch-as-code.sh

cat <<EOM




Demo folder created. To cd there, run:
   cd $(pwd)
Run ./arch-as-code.sh as an alias for the executable
EOM
