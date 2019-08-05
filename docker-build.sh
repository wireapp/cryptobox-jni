#!/usr/bin/env bash
set -e

if [[ "$1" = "--clean" ]]; then
	# clean stopped containers
	docker rm `docker ps -aq`

	# clean dangling images
	docker rmi `docker images --filter "dangling=true" -q`
fi

IMAGE_NAME="wire/cryptobox-jni"

# build
docker build -t ${IMAGE_NAME} .
docker create -ti --name temp_build ${IMAGE_NAME} bash

# archive
rm -fr output || true
mkdir -p output
docker cp temp_build:/home/rust/cryptobox-jni/android/dist output/
echo "DONE: output is in `pwd`/output"