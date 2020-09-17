# Arch as code

[![CI](https://github.com/trilogy-group/arch-as-code/workflows/CI/badge.svg?branch=master)](https://github.com/trilogy-group/arch-as-code/actions)
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
* [Build maintenance](#build-maintenance)
* [The demo folder](#the-demo-folder)
* [TODOs](#todo)

## Getting started

### 0. Use Java 11 locally

The build currently assumes Java 11.  Several tools exist to manage multiple
JDK versions.  A good choice is [jEnv](https://www.jenv.be/).  See the
[section on jEnv](#jenv).

If unsure of installing a current JDK patch for version 11, use
[AdoptOpenJdk](https://adoptopenjdk.net/).

### 1. Use IntelliJ

Use a 2020+ version of "Ultimate" edition.  "Community Edition" may work, but
YMMV.

Ensure your Project Structure is using Java 11.

Install these plugins:

* Lombok

Ensure that underneath the "Setting"/"Preferences" of 
"Build, Execution, Deployment|Compiler|Annotation Processors" that "Enable
annotation processing" is enabled.

### 2. Create Structurizr account

First you'll need to create a Structurizr account. You can do this by
following the Structurizr
[getting started](https://structurizr.com/help/getting-started) guide
that describes how to set up a new account and get a **free** workspace.

### 3. Save credentials files to your home directory

```shell script
$ cd  # Take you home from where you are
$ mkdir .arch-as-code/{google,jira,structurizer}
```

Save each credential file type to it's related directory name.  Then:

```shell script
$ chmod -R go= .arch-as-code  # Credentials should be readable only by you
```

### 4. Install arch-as-code CLI

Arch as code requires Java 11 or greater to be installed.

You can download the latest arch-as-code tarball
[here](https://github.com/trilogy-group/arch-as-code/releases/latest) or
you can run commands for your respective OS below to install the latest
version of arch-as-code CLI.

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

Next we'll initialize a new local workspace to store our architecture
assets as code.

In order to do this you'll need to retrieve your Structurizr
WORKSPACE_ID, WORKSPACE_API_KEY and WORKSPACE_API_SECRET from the
Structurizr account
[dashboard](https://structurizr.com/dashboard).<!-- @IGNORE PREVIOUS: link -->

Then you can then run the following command to initialize your workspace
(PATH_TO_WORKSPACE refers to workspace directory).

```shell script
mkdir -p ${PATH_TO_WORKSPACE}

cd ${PATH_TO_WORKSPACE}

arch-as-code init -i ${WORKSPACE_ID} -k ${WORKSPACE_API_KEY} -s ${WORKSPACE_API_SECRET} .
```

### 5. Publish to Structurizr

We can now publish our local workspace to Structurizr using the
following command:

```shell script
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
See [batect.yml](./batect.yml) to update the Docker image used, and the
target commands for the command line.

Use `./batect -T` to list available targets.  Currently:
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
Trilogy email with no `@` suffix, and your Jira password.  An example:
`brian.oxley` rather than `brian.oxley@trilogy.com`.

## TODO

* Instructions for obtaining various credential files
* Migrate to JUnit 5
* Use [System Rules](https://stefanbirkner.github.io/system-rules/) rather
  than tests manually manipulating `System.out` and `System.err`
* Use of modern assertions such as
  [AssertJ](https://github.com/joel-costigliola/assertj-core), et al
