#!/bin/bash

set -e

VERSION_FILE="mk/version.mk"
CBOX_VERSION=`grep VERSION ${VERSION_FILE} | sed 's/VERSION\ := \(.*\)/\1/' | sed 's/\s//'`
if [[ $CBOX_VERSION =~ ^[0-9]\.[0-9]\.[0-9]$ ]]; then
	echo "Version from '${VERSION_FILE}' is: ${CBOX_VERSION}"
else
	>&2 echo "Can't parse version in ${VERSION_FILE}"
	exit 1
fi

# Create POM file
sed s/%%VERSION%%/${CBOX_VERSION}/ TEMPLATE.pom > output/dist/cryptobox-android-${CBOX_VERSION}.pom

# Create version
jfrog bt vc wire-android/releases/cryptobox-android/${CBOX_VERSION}
# Upload files
jfrog bt u "output/dist/cryptobox-android-${CBOX_VERSION}.aar" \
	"wire-android/releases/cryptobox-android/${CBOX_VERSION}" \
	"com/wire/cryptobox-android/${CBOX_VERSION}/"
jfrog bt u "output/dist/cryptobox-android-${CBOX_VERSION}.pom" \
	"wire-android/releases/cryptobox-android/${CBOX_VERSION}" \
	"com/wire/cryptobox-android/${CBOX_VERSION}/"
# Publish
jfrog bt vp wire-android/releases/cryptobox-android/${CBOX_VERSION}

echo "Version ${CBOX_VERSION} uploaded!"