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

## Table of Contents

* [Getting started](#getting-started)
* [Building locally](#building-locally)

## Getting started

### 1. Create Structurizr account

First you'll need to create a Structurizr account. You can do this by
following the Structurizr
[getting started](https://structurizr.com/help/getting-started) guide
that describes how to set up a new account and get a **free** workspace.

### 2. Use Java 11 locally

The build currently assumes Java 11.  Several tools exist to manage multiple
JDK versions.  A good choice is [jEnv](https://www.jenv.be/).  See the
[section on jEnv](#jenv).

### 3. Install arch-as-code CLI

Arch as code requires Java 11 or greater to be installed.

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
Invoke-Expression "& { $(Invoke-RestMethod -Uri https://raw.githubusercontent.com/trilogy-group/arch-as-code/master/scripts/install/windows/install.ps1 -Headers @{"Cache-Control"="no-cache"} ) }"

arch-as-code --help
```

### 4. Initialize local workspace

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

### 5. Publish to Structurizr

We can now publish our local workspace to Structurizr using the
following command:

```bash
cd ${PATH_TO_WORKSPACE}

arch-as-code publish .
```

### 6. View changes on Structurizr

Once you've published your changes, you and others can view your
architecture assets online through your previously created Structurizr
workspace (https://structurizr.com/workspace/${WORKSPACE_ID}).

## Building locally

### Setup

#### jEnv

Recommended is [jEnv](https://www.jenv.be/) for local builds Linux or MacOS.
This tool sets up your local environment to use the version of Java
relevant to your project&mdash;in this case, 11&mdash;without you needing
to manually update `PATH` or `JAVA_HOME`.

After following instructions, the AaC repository should be set up for you
as Java 11:

```bash
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
See [batect.yml](./batect.yml) to update the Docker image used, and the
target commands for the command line.

Use `./batect -T` to list available targets.  Currently:
```bash
$ ./batect -T
Available tasks:
- build: Build (and test) the program
```
Batect should share local Gradle downloads with the Docker container.

#### Code coverage

To view HTML coverage reports, use:
```
$ (cd build/reports/jacoco/test/html/; open index.html)
```
The subshell syntax is to avoid changing your current terminal directory.
The `open` command is MacOS-specific; for Linux, use `alias open=xdg-open`.

## Build maintenance

Use `./gradlew dependencyUpdates` for a list of out-of-date dependencies and
plugins, or Gradle itself.
