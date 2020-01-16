# Arch as code

![](https://github.com/nahknarmi/arch-as-code/workflows/Build%20&%20Test/badge.svg) 
[![Test Coverage](https://api.codeclimate.com/v1/badges/bf154787f36e5afed62e/test_coverage)](https://codeclimate.com/github/nahknarmi/arch-as-code/test_coverage)
[![Maintainability](https://api.codeclimate.com/v1/badges/bf154787f36e5afed62e/maintainability)](https://codeclimate.com/github/nahknarmi/arch-as-code/maintainability)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=nahknarmi/arch-as-code)](https://dependabot.com)
[![Known Vulnerabilities](https://snyk.io/test/github/nahknarmi/arch-as-code/badge.svg)](https://snyk.io/test/github/nahknarmi/arch-as-code)

Arch as code project is to manage **product architecture as code**. 

By following this approach we will be able to **manage our architecture documents, models, decisions and diagrams** in the same way we do code and gain all the **tools and workflows supporting modern development**. Think PR reviews, static code analysis, continuous integration & continuous deployment.

Specifically we are making use of the [Structurizr](https://structurizr.com/) tool by Simon Brown as the basis for structuring and storing our architecture models, decisions, views and documentation.

# Getting Started 

## 0. Create Structurizr Account

See Structurizr [getting started](https://structurizr.com/help/getting-started) guide on how to setup a new account and get a free workspace.

## 1. Install arch-as-code cli

Arch as code required java 8 or greater to be installed.

### Mac OSX & Linux

```bash
mkdir -p ~/arch-as-code && curl -s https://api.github.com/repos/nahknarmi/arch-as-code/releases/latest | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | xargs curl -L | tar --strip-components 1 -x -C ~/arch-as-code

export PATH=$PATH:~/arch-as-code/bin

arch-as-code --help
```

### Windows

Download the latest binary [here](https://github.com/nahknarmi/arch-as-code/releases/latest).

```powershell
TBD
```


## 2. Configure Structurizr workspace details

Initialize arch-as-code using information available Structurizr workspace (available from [dashboard]([dashboard](https://structurizr.com/dashboard))):

```
mkdir -p <PATH_TO_STORE_ARCHITECTURE_DOCUMENTATION>

arch-as-code init -i <WORKSPACE_ID> -k <WORKSPACE_API_KEY> -s <WORKSPACE_API_SECRET> <PATH_TO_STORE_ARCHITECTURE_DOCUMENTATION>
```

## 3. Publish changes to Structurizr
```
arch-as-code publish <PATH_TO_STORE_ARCHITECTURE_DOCUMENTATION>
```

## 4. View your changes on Structurizr

Go to [https://structurizr.com/workspace/<YOUR_WORKSPACE_ID>] to view changes you've made.

# Development

## Build Pre-requisites
- Java 1.8 or greater.
- Create [Structurizr](https://structurizr.com/) credentials file under `.arch-as-code/structurizr/credentials.json`. 
You can find sample file under `src/main/resources/sample_credentials.json` and update with contents. 
You can find workspaces specific keys from [https://structurizr.com/dashboard](https://structurizr.com/) 

## Build

Builds and tests application code for publishing architecture data structure to Structurizr.

Tests operate against a "test" Structurizr workspace.

```bash
./gradlew build
```

## Continuous Integration & Continuous Deployment

Continuous integration is currently being done using [GitHub Actions](https://github.com/nahknarmi/arch-as-code/actions) (will change soon).

Continuous deployment (publishing documentation) is currently being done using [GitHub Actions](https://github.com/nahknarmi/arch-as-code/actions) (will change soon). 

GitHub Actions configuration is captured under `.github/workflows/`

## Structurizr Notes
- In order to get the API keys & secret you need to first create a workspace
- [Structurizr Java example](https://github.com/structurizr/java-quickstart)
- [API documentation](https://structurizr.com/help/web-api)
- [Structurizr open API docs](https://structurizr.com/static/assets/structurizr-api.yaml)
