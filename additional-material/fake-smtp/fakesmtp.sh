#!/bin/bash
set -euo pipefail
IFS=$'\t\n'

if [ ! -f fakesmtp.jar ]; then
    wget http://nilhcem.github.com/FakeSMTP/downloads/fakeSMTP-latest.zip
    unzip fakeSMTP-latest.zip
    mv fakeSMTP-2.0.jar fakesmtp.jar
    rm fakeSMTP-latest.zip
fi

sudo java -jar fakesmtp.jar -s -o maildir
