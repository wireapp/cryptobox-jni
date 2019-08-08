#!/usr/bin/env bash

# -u root
docker run --security-opt seccomp:unconfined -it --rm wire/cryptobox-jni
