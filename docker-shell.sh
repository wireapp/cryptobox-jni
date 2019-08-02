#!/usr/bin/env bash

docker run --security-opt seccomp:unconfined -it --rm wire/cryptobox-jni
