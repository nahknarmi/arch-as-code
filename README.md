# Arch as code

![](https://github.com/trilogy-group/arch-as-code/workflows/Build%20&%20Test/badge.svg)
[![Test Coverage](https://api.codeclimate.com/v1/badges/bf154787f36e5afed62e/test_coverage)](https://codeclimate.com/github/trilogy-group/arch-as-code/test_coverage)
[![Maintainability](https://api.codeclimate.com/v1/badges/bf154787f36e5afed62e/maintainability)](https://codeclimate.com/github/trilogy-group/arch-as-code/maintainability)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=trilogy-group/arch-as-code)](https://dependabot.com)
[![Known Vulnerabilities](https://snyk.io/test/github/trilogy-group/arch-as-code/badge.svg)](https://snyk.io/test/github/trilogy-group/arch-as-code)

**Arch as code** is an approach for managing **software architecture as
code**.

By following this approach we will be able to **manage our architecture
documents, models, decisions and diagrams** in the same way we do code
thus benefiting from all **tools, techniques and workflows supporting
modern development**. Think PR reviews, static code analysis, continuous
integration & continuous deployment.

Specifically we are making use of the
[Structurizr](https://structurizr.com/) tool by Simon Brown as the basis
for structuring and storing our architecture models, decisions, views
and documentation.

## Getting Started

### 0. Create Structurizr account

First you'll need to create a Structurizr account. You can do this by
follow the Structurizr
[getting started](https://structurizr.com/help/getting-started) guide
that describes how to setup a new account and get a **free** workspace.

### 1. Install arch-as-code CLI

Arch as code requires Java 8 or greater to be installed.

You can download the latest arch-as-code tarball
[here](https://github.com/trilogy-group/arch-as-code/releases/latest) or
you can run commands for your respective OS below to install the latest
version of arch-as-code CLI.

#### Mac OS

```bash
mkdir -p ~/arch-as-code && curl -s https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | xargs curl -L | tar --strip-components 1 -x -C ~/arch-as-code

export PATH=$PATH:~/arch-as-code/bin

arch-as-code --help
```

#### Linux

```bash
mkdir -p ~/arch-as-code && curl -s https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | xargs curl -L | tar -z --strip-components 1 -x -C ~/arch-as-code

export PATH=$PATH:~/arch-as-code/bin

arch-as-code --help
```

#### Windows

```powershell
$download_url = (Invoke-WebRequest "https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest" | ConvertFrom-Json).assets.browser_download_url

(New-Object System.Net.WebClient).DownloadFile($download_url, "$env:temp\arch-as-code.tar.gz")  

New-Item -ItemType Directory -Force -Path $HOME\arch-as-code

tar -xzv --strip-components 1 -f $env:temp\arch-as-code.tar.gz -C $HOME\arch-as-code\

$Env:Path += ";$HOME\arch-as-code\bin"

arch-as-code --help
```


### 2. Initialize local workspace

Next we'll initialize a new local workspace to store our architecture
assets as code.

In order to do this you'll need to retrieve your Structurizr
WORKSPACE_ID, WORKSPACE_API_KEY and WORKSPACE_API_SECRET from the
Structurizr account
[dashboard](https://structurizr.com/dashboard).<!-- @IGNORE PREVIOUS: link -->

Then you can then run the following command to initialize your workspace
(PATH_TO_WORKSPACE refers to workspace directory).

```bash
mkdir -p ${PATH_TO_WORKSPACE}

cd ${PATH_TO_WORKSPACE}

arch-as-code init -i ${WORKSPACE_ID} -k ${WORKSPACE_API_KEY} -s ${WORKSPACE_API_SECRET} .
```

### 3. Publish to Structurizr

We can now publish our local workspace to Structurizr using the
following command:

```bash
cd ${PATH_TO_WORKSPACE}

arch-as-code publish .
```

### 4. View changes on Structurizr

Once you've published your changes, you and others can view your
architecture assets online through your previously created Structurizr
workspace (https://structurizr.com/workspace/${WORKSPACE_ID}).

## Development

### Build Pre-requisites

- Java 8 or greater.
- Create [Structurizr](https://structurizr.com/) credentials file under
  `.arch-as-code/structurizr/credentials.json`. You can find sample file
  under `src/main/resources/sample_credentials.json` and update with
  contents. You can find workspaces specific keys from
  [https://structurizr.com/dashboard](https://structurizr.com/dashboard)

### Build

Builds and tests application code for publishing architecture data
structure to Structurizr.

Tests operate against a "test" Structurizr workspace.

```bash
./gradlew build
```

## Continuous Integration & Continuous Deployment

Continuous integration is currently being done using
[GitHub Actions](https://github.com/trilogy-group/arch-as-code/actions).

Continuous deployment (publishing documentation) is currently being done
using
[GitHub Actions](https://github.com/trilogy-group/arch-as-code/actions).

GitHub Actions configuration is captured under `.github/workflows/`

## Structurizr notes

- [Structurizr Java example](https://github.com/structurizr/java-quickstart)
- [API documentation](https://structurizr.com/help/web-api)
- [Structurizr open API docs](https://structurizr.com/static/assets/structurizr-api.yaml)

