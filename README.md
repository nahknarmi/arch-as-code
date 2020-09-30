# Arch as code

[![CI](https://github.com/trilogy-group/arch-as-code/workflows/CI/badge.svg?branch=master)](https://github.com/trilogy-group/arch-as-code/actions)
[![Test Coverage](https://api.codeclimate.com/v1/badges/bf154787f36e5afed62e/test_coverage)](https://codeclimate.com/github/trilogy-group/arch-as-code/test_coverage)
[![Maintainability](https://api.codeclimate.com/v1/badges/bf154787f36e5afed62e/maintainability)](https://codeclimate.com/github/trilogy-group/arch-as-code/maintainability)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=trilogy-group/arch-as-code)](https://dependabot.com)
[![Known Vulnerabilities](https://snyk.io/test/github/trilogy-group/arch-as-code/badge.svg)](https://snyk.io/test/github/trilogy-group/arch-as-code)

**Arch as code** is an approach for managing **architecture as code** for
software projects. This means C4 modeling and relationships to story cards in
Jira.

By following this approach we will be able to **manage our architecture
documents, models, decisions and diagrams** in the same way we do code thus
benefiting from all **tools, techniques and workflows supporting modern
development**. Think PR reviews, static code analysis, continuous integration
& continuous deployment.

Specifically we are making use of the
[Structurizr](https://structurizr.com/) tool by Simon Brown as the basis for
structuring and storing our architecture models, decisions, views and
documentation.

## Table of Contents

* [Getting started](#getting-started)
* [Building locally](#building-locally)
* [Developing](#developing)
* [Build maintenance](#build-maintenance)
* [The demo folder](#the-demo-folder)
* [TODOs](#todo)

## Getting started

### 0. Use Java 11 locally

The build currently assumes Java 11. Several tools exist to manage multiple
JDK versions. A good choice for Linux or MacOS is
[jEnv](https://www.jenv.be/). The project includes a "dot" file to set your
build to Java 11 when in the project root.

If unsure about of installing a current JDK version for version 11, use
[AdoptOpenJdk](https://adoptopenjdk.net/).

### 1. Use IntelliJ

Use a 2020+ version of "Ultimate" edition.  "Community Edition" may work, but
YMMV. This is *not* a requirement: it is a suggestion.

Ensure your Project Structure is using Java 11.

Install these plugins:

* Lombok

Ensure that underneath the "Setting"/"Preferences" of
"Build, Execution, Deployment|Compiler|Annotation Processors" that "Enable
annotation processing" is enabled.

### 2. Structurizr account

First you will need a Structurizr account. If you do not have one, you can
create one by following the Structurizr
[getting started](https://structurizr.com/help/getting-started) guide that
describes how to set up a new account and get a **free** workspace.

### 3. Save credentials files to your home directory

```shell script
cp -a <project root>/samples/config ~/.arch-as-code
chmod -R go= ~/.arch-as-code
```

These files *need updating* to match your actual credentials.

For Google, log into the
[Google Console](https://console.developers.google.com/), pick
"arch-as-code" from the project dropdown, and download a credentials JSON
file. For Structurizer, log into Structurizer, and find your API key and
secret.

### 4. Install arch-as-code CLI

Arch as code requires Java 11.
Consider [AdoptOpenJDK](https://adoptopenjdk.net/) if you do not have a JDK 11
installed.

#### Mac OS

```shell script
mkdir -p ~/arch-as-code && curl -s https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | xargs curl -L | tar --strip-components 1 -x -C ~/arch-as-code

export PATH=$PATH:~/arch-as-code/bin

arch-as-code --help
```

#### Linux

```shell script
mkdir -p ~/arch-as-code && curl -s https://api.github.com/repos/trilogy-group/arch-as-code/releases/latest | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | xargs curl -L | tar -z --strip-components 1 -x -C ~/arch-as-code

export PATH=$PATH:~/arch-as-code/bin

arch-as-code --help
```

#### Windows

```powershell
Invoke-Expression "& { $(Invoke-RestMethod -Uri https://raw.githubusercontent.com/trilogy-group/arch-as-code/master/scripts/install/windows/install.ps1 -Headers @{"Cache-Control"="no-cache"} ) }"

arch-as-code --help
```

### 4. Initialize local workspace

Next we'll initialize a new local workspace to store our architecture assets
as code.

In order to do this you'll need to retrieve your Structurizr WORKSPACE_ID,
WORKSPACE_API_KEY and WORKSPACE_API_SECRET from the Structurizr account
[dashboard](https://structurizr.com/dashboard).<!-- @IGNORE PREVIOUS: link -->

Then you can then run the following command to initialize your workspace
(PATH_TO_WORKSPACE refers to workspace directory).

```shell script
mkdir -p ${PATH_TO_WORKSPACE}

cd ${PATH_TO_WORKSPACE}

arch-as-code init -i ${WORKSPACE_ID} -k ${WORKSPACE_API_KEY} -s ${WORKSPACE_API_SECRET} .
```

### 5. Publish to Structurizr

We can now publish our local workspace to Structurizr using the following
command:

```shell script
cd ${PATH_TO_WORKSPACE}

arch-as-code publish .
```

### 6. View changes on Structurizr

Once you've published your changes, you and others can view your architecture
assets online through your previously created Structurizr
workspace (https://structurizr.com/workspace/${WORKSPACE_ID}).

## Building locally

### Setup

#### jEnv

Recommended is [jEnv](https://www.jenv.be/) for local builds Linux or MacOS.
This tool sets up your local environment to use the version of Java relevant
to your project&mdash;in this case, 11&mdash;without you needing to manually
update `PATH` or `JAVA_HOME`.

After following instructions, the AaC repository should be set up for you as
Java 11:

```shell script
$ cd <your root of the AaC project git clone>
$  java -version
<output which indicates a build of Java 11>
```

### Building

Use `./gradlew` (Gradle) or `./batect build` (Batect) to build or run tests.

Batect runs `./gradlew` inside a Docker container against the current git
clone project root, and should _always_ produce the same results.

#### Batect

[Batect](https://batect.dev/) is a local script which runs your project in
Docker, similar to your CI system, provided by Charles Korn, a ThoughtWorker.
This ensures your local build is as close to CI and Production as possible.
See [batect.yml](./batect.yml) to update the Docker image used, and the target
commands for the command line.

Use `./batect -T` to list available targets. Currently:

```shell script
$ ./batect -T
Available tasks:
- build: Build (and test) the program
```

Batect should share local Gradle downloads with the Docker container.

#### Code coverage

To view HTML coverage reports, use:

```shell script
$ (cd build/reports/jacoco/test/html/; open index.html)
```

The subshell syntax is to avoid changing your current terminal directory.
The `open` command is MacOS-specific; for Linux, setup `alias open=xdg-open`,
then you can use `open`.

## Developing

### Build Pre-requisites

- JDK 11 or greater
- Create [Structurizr](https://structurizr.com/) credentials file under
  `.arch-as-code/structurizr/credentials.json`. You can find sample file
  under `src/main/resources/sample_credentials.json` and update with contents.
  You can find workspaces specific keys from
  [https://structurizr.com/dashboard](https://structurizr.com/dashboard)

### Build

Builds and tests application code for publishing architecture data structure
to Structurizr.

Tests operate against a "test" Structurizr workspace.

```bash
./gradlew build
```

### Run

Runs Bootstrap.java. Equivalent to executing the binary of a release.

```bash
./gradlew run --args='-h'
```

For example, to initialize a workspace, run:

```bash
mkdir /tmp/temporaryWorkSpace
./gradlew run --args="init -i ${WORKSPACE_ID} -k ${WORKSPACE_API_KEY} -s ${WORKSPACE_API_SECRET} /tmp/temporaryWorkSpace"
```

## Continuous Integration & Continuous Deployment

Continuous integration is currently being done using
[GitHub Actions](https://github.com/trilogy-group/arch-as-code/actions).

Continuous deployment (publishing documentation) is currently being done using
[GitHub Actions](https://github.com/trilogy-group/arch-as-code/actions).

GitHub Actions configuration is captured under `.github/workflows/`

## Structurizr notes

- [Structurizr Java example](https://github.com/structurizr/java-quickstart)
- [API documentation](https://structurizr.com/help/web-api)
- [Structurizr open API docs](https://structurizr.com/static/assets/structurizr-api.yaml)

## Project Configuration Files

Configuration files are required to run the application and run unit tests.

All configuration files are stored under .arch-as-code folder int the root of
the project. Copy from `samples/config`.

### Jira

A typical `.arch-as-code/jira/settings.json` is:

```
{
  "base_uri": "https://jira.devfactory.com"
}
``` 

### Structurizr

A typical `.arch-as-code/structurizr/credential.json` is:

```
{
  "workspace_id": "49344",
  "workspace_name": "master",
  "api_key": "<Ask about the key>",
  "api_secret": "<Ask about the secret>"
}
```

### Google API

Ask to be added to the google api project using google console

- Go to https://console.developers.google.com/
- Log in with your Trilogy Account
- Select project `Arch-as-Code`
- Click on Credentials
- Under `OAuth 2.0 Client IDs` there is a name `arc-as-code` if it doesn't
  exist create a new client Id
- Click on the copy key or the edit icon, or the download icon to get the key
  and secret.
- Save the downloaded key from console into the file
  .arch-as-code/google/client_secret.json

A typical `.arch-as-code/google/client_secret.json` is:

```
{
  "installed": {
    "client_id": "<Your key form google console>",
    "project_id": "arch-as-code",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "<Your secret from google console>",
    "redirect_uris": [
      "urn:ietf:wg:oauth:2.0:oob",
      "http://localhost"
    ]
  }
}
```

## Build maintenance

Use `./gradlew dependencyUpdates` for a list of out-of-date dependencies and
plugins, or Gradle itself.

## The demo folder

To create a local demo folder for AaC, run:

```shell script
$ ./scripts/create-demo-folder.sh
$ pushd /tmp/aac/demo-folder
$ alias aac=$PWD/arch-as-code.sh
# Proceed using the command-line as `aac ...`
```

Until we get upstream to add "technology" lines to "product-architect.yml",
you will need to _manually_ edit before validation passes.

### Credentials

When prompted for Jira credentials, such as publishing stories, use your
Trilogy email with no `@` suffix, and your Jira password. An example:
`brian.oxley` rather than `brian.oxley@trilogy.com`.

See `samples/config` for example files to place under your `~/.arch-as-code`
directory.

## TODO

* Instructions for obtaining various credential files
* Migrate to JUnit 5
* Use [System Rules](https://stefanbirkner.github.io/system-rules/) rather
  than tests manually manipulating `System.out` and `System.err`
* Use of modern assertions such as
  [AssertJ](https://github.com/joel-costigliola/assertj-core), et al
