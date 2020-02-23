Set-PSDebug -Trace 1

$download_url = (Invoke-WebRequest "https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest" | ConvertFrom-Json).assets.browser_download_url

(New-Object System.Net.WebClient).DownloadFile($download_url, "$env:temp\arch-as-code.tar.gz")

New-Item -ItemType Directory -Force -Path $HOME\arch-as-code

tar -xzv --strip-components 1 -f $env:temp\arch-as-code.tar.gz -C $HOME\arch-as-code\

$Env:Path += ";$HOME\arch-as-code\bin"


arch-as-code --help

New-Item -ItemType Directory -Force -Path "$env:temp\my-awesome-product"
cd "$env:temp\my-awesome-product"

ls "$env:temp"

ls "$env:temp\my-awesome-product"

pwd

arch-as-code --version

arch-as-code init -i "$STRUCTURIZR_WORKSPACE_ID" -k "$STRUCTURIZR_API_KEY" -s "$STRUCTURIZR_API_SECRET" .

