#!/bin/bash
set -eux

if [[ "$OSTYPE" == "linux-gnu" ]]; then
    curl -L https://codeclimate.com/downloads/test-reporter/test-reporter-latest-linux-amd64 > /tmp/test-reporter
elif [[ "$OSTYPE" == "darwin"* ]]; then
    curl -L https://codeclimate.com/downloads/test-reporter/test-reporter-latest-darwin-amd64 > /tmp/test-reporter
else
    echo "Unsupported OS"
    exit 1
fi

chmod +x /tmp/test-reporter

./gradlew jacocoTestReport

JACOCO_SOURCE_PATH=src/main/java /tmp/test-reporter format-coverage build/reports/jacoco/test/jacocoTestReport.xml --input-type jacoco

/tmp/test-reporter upload-coverage -r 3981b00132185321e3e9764462878f8da1152dd73246f8e04a4cb4b0dcfaba73