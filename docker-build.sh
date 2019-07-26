#!/bin/bash

if [[ "$1" = "--clean" ]]; then
	# clean stopped containers
	docker rm `docker ps -aq`

	# clean dangling images
	docker rmi `docker images --filter "dangling=true" -q`
fi

# build
docker build -t wire/cryptobox-jni .