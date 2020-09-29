## Developing

### Build Pre-requisites

- JDK 11 or greater
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

Continuous deployment (publishing documentation) is currently being done
using
[GitHub Actions](https://github.com/trilogy-group/arch-as-code/actions).

GitHub Actions configuration is captured under `.github/workflows/`

## Structurizr notes

- [Structurizr Java example](https://github.com/structurizr/java-quickstart)
- [API documentation](https://structurizr.com/help/web-api)
- [Structurizr open API docs](https://structurizr.com/static/assets/structurizr-api.yaml)


## Project Configuration Files

Cconfiguration files are required to run the application and run unit tests.

All configuration files are stored under .arch-as-code folder int the root of the project

### Jira

Create .arch-as-code/jira/settings.json
```
{
  "base_uri": "https://jira.devfactory.com",
  "get_story_endpoint": "/rest/api/2/issue/",
  "bulk_create_endpoint": "/rest/api/2/issue/bulk",
  "link_prefix": "/browse/"
}
``` 

### Structurizr

Create .arch-as-code/structurizr/credential.sjon

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
- Under `OAuth 2.0 Client IDs` there is a name `arc-as-code` if it doesn't exist create a new client Id
- Click on the copy key or the edit icon, or the download icon to get the key and secret.
- Save the downloaded key from console into the file .arch-as-code/google/client_secret.json
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
