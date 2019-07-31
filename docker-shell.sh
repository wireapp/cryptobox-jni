#!/bin/bash

docker run --security-opt seccomp:unconfined -it --rm -v $(pwd):/root wire/cryptobox-jni