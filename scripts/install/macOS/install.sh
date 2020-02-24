#!/usr/bin/env bash
set -eux

curl -i https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest

curl -v https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest

mkdir -p ~/arch-as-code && curl -s https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | xargs curl -L | tar --strip-components 1 -x -C ~/arch-as-code

export PATH=$PATH:~/arch-as-code/bin

arch-as-code --help

mkdir -p ~/my-awesome-product
cd ~/my-awesome-product

arch-as-code init -i "${STRUCTURIZR_WORKSPACE_ID}" -k "${STRUCTURIZR_API_KEY}" -s "${STRUCTURIZR_API_SECRET}" .

arch-as-code validate .

arch-as-code publish .

