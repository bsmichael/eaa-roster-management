#!/bin/sh
# See https://stackoverflow.com/questions/14825039/suppressing-gpg-signing-for-maven-based-continuous-integration-builds-travis-ci
mvn -DperformRelease=true clean install
