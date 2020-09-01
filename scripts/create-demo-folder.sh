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
   -C, --cleanup    Remove temp demo folders
EOH
}

# shellcheck disable=SC1090
. "${0%/*}/colors.sh"
export TTY=false
[[ -t 1 ]] && TTY=true

tmpdir="${TMPDIR-/tmp}/aac-$$"
# A trick to show output on failure, but not on success
outfile="$tmpdir/out"

function cleanup-temp-folders() {
    local rc=0
    # Try to remove as many as possible; if any fail, let rm print the error,
    # and continue to the next folder.
    # If there are no temp folders, this becomes a no-op
    while read -r f; do
        rm -r "$f" && echo "Removed $f" || rc=1
    done < <(ls -d "${TMPDIR-/tmp}"/aac-* 2>/dev/null)
    return $rc
}

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
while getopts :Cch-: opt; do
    [[ $opt == - ]] && opt=${OPTARG%%=*} OPTARG=${OPTARG#*=}
    case $opt in
    C | cleanup)
        cleanup-temp-folders || exit 1
        exit 0
        ;;
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

run ./gradlew clean # Start clean

cp ./scripts/demo-git-ignore "$tmpdir"/demo-folder/.gitignore
cp ./.java-version "$tmpdir"/demo-folder

run ./gradlew bootJar
mkdir -p "$tmpdir"/demo-folder/.install/bin
cp ./build/libs/arch-as-code-*.jar "$tmpdir"/demo-folder/.install/bin

cat <<EOS >"$tmpdir"/demo-folder/.install/bin/arch-as-code
#!/usr/bin/env bash

exec java -jar "$tmpdir"/demo-folder/.install/bin/arch-as-code-*.jar "\$@"
EOS
chmod a+rx "$tmpdir"/demo-folder/.install/bin/arch-as-code

cd "$tmpdir"/demo-folder

run git init

pwd  # Tell the user where to find the demo folder

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
