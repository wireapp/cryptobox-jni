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
rm -rf output || true
mkdir -p output
chmod o+rw output
docker run -v ./output:/home/rust/cryptobox-jni/android/dist ${IMAGE_NAME} make dist
echo "DONE: output is in `pwd`/output"
