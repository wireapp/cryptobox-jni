SHELL   := /usr/bin/env bash

ANDROID_NDK_HOME              ?=

OS       := $(shell uname -s | tr '[:upper:]' '[:lower:]')
ARCH     := $(shell uname -m)
export HOST_TAG=${OS}-${ARCH}
export TOOLCHAIN="${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${HOST_TAG}"

include ../mk/version.mk

# pkg-config is invoked by libsodium-sys
# cf. https://github.com/alexcrichton/pkg-config-rs/blob/master/src/lib.rs#L12
export PKG_CONFIG_ALLOW_CROSS=1

# Hint for sodiumoxide to find the precompiled libsodium library
# See the README at  https://github.com/sodiumoxide/sodiumoxide
export SODIUM_LIB_DIR="./libs"

.PHONY: all
all: compile

.PHONY: clean
clean:
	$(ANDROID_NDK_HOME)/ndk-build clean &> /dev/null || true
	rm -rf libs

.PHONY: compile
compile: cryptobox
	$(ANDROID_NDK_HOME)/ndk-build
	mkdir -p build/classes
	javac -source 1.6 -target 1.6 -d build/classes ../src/java/com/wire/cryptobox/*.java

.PHONY: doc
doc:
	mkdir -p dist/javadoc
	javadoc -Xdoclint:none -public -d dist/javadoc ../src/java/com/wire/cryptobox/*.java

.PHONY: distclean
distclean:
	rm -rf build
	rm -rf dist
	rm -rf jni/armeabi-v7a
	rm -rf jni/arm64-v8a
	rm -rf jni/x86_64
	rm -rf jni/x86
	rm -rf jni/include

.PHONY: dist-libs
dist-libs:
	mkdir -p dist/lib
	cp -r libs/* dist/lib/

dist/cryptobox-jni-$(VERSION).jar: compile
	mkdir -p dist
	jar -cvf dist/cryptobox-jni-$(VERSION).jar -C build/classes .

.PHONY: dist-jar
dist-jar: dist/cryptobox-jni-$(VERSION).jar

dist/cryptobox-android-$(VERSION).tar.gz: dist-libs dist-jar doc
	tar -C dist \
		-czf dist/cryptobox-android-$(VERSION).tar.gz \
		lib javadoc cryptobox-jni-$(VERSION).jar

.PHONY: dist-tar
dist-tar: dist/cryptobox-android-$(VERSION).tar.gz

dist/cryptobox-android-$(VERSION).aar: dist-libs dist-jar
	mkdir -p dist/aar/jni
	cp dist/cryptobox-jni-$(VERSION).jar dist/aar/classes.jar
	cp AndroidManifest.xml dist/aar/
	cp -r dist/lib/* dist/aar/jni/
	cd dist/aar && zip -r ../cryptobox-android-$(VERSION).aar *
	rm -rf dist/aar

.PHONY: dist-aar
dist-aar: dist/cryptobox-android-$(VERSION).aar

.PHONY: dist
dist: compile dist-tar dist-aar

#############################################################################
# cryptobox

include ../mk/cryptobox-src.mk

.PHONY: cryptobox
cryptobox: jni/armeabi-v7a/libcryptobox.so jni/include/cbox.h

jni/armeabi-v7a/libcryptobox.so: libsodium-armeabi-v7a | build/src/$(CRYPTOBOX_NAME)
	cd build/src/$(CRYPTOBOX_NAME) && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export SODIUM_LIB_DIR=../../libsodium-android-armv7-a/lib && \
	export SODIUM_SHARED=1 && \
	cargo rustc --lib --release --target=armv7-linux-androideabi -- \
	               -C ar=arm-linux-androideabi-ar \
	               -C linker=armv7a-linux-androideabi16-clang 
	mkdir -p jni/armeabi-v7a
	cp build/src/$(CRYPTOBOX_NAME)/target/armv7-linux-androideabi/release/libcryptobox.so jni/armeabi-v7a/libcryptobox.so



jni/include/cbox.h: | build/src/$(CRYPTOBOX_NAME)
	mkdir -p jni/include
	cp build/src/$(CRYPTOBOX_NAME)/src/cbox.h jni/include/

#############################################################################
# libsodium

include ../mk/libsodium-src.mk

.PHONY: libsodium-armeabi-v7a
libsodium-armeabi-v7a: jni/armeabi-v7a/libsodium.so

.PHONY: libsodium
libsodium: jni/armeabi-v7a/libsodium.sox

jni/armeabi-v7a/libsodium.so: | build/src/$(LIBSODIUM_NAME)
	cd build/src/$(LIBSODIUM_NAME) && \
	export CFLAGS="-Os -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -marm -march=armv7-a" && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export AR=${TOOLCHAIN}/bin/arm-linux-androideabi-ar && \
	export AS=${TOOLCHAIN}/bin/arm-linux-androideabi-as && \
	export CC=${TOOLCHAIN}/bin/armv7a-linux-androideabi16-clang && \
	export CXX=${TOOLCHAIN}/bin/armv7a-linux-androideabi16-clang++ && \
	export LD=${TOOLCHAIN}/bin/arm-linux-androideabi-ld && \
	export RANLIB=${TOOLCHAIN}/bin/arm-linux-androideabi-ranlib && \
	export STRIP=${TOOLCHAIN}/bin/arm-linux-androideabi-strip && \
	./configure --host=arm-linux-androideabi \
	            --with-sysroot="${TOOLCHAIN}/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-armv7-a" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/armeabi-v7a
	cp build/libsodium-android-armv7-a/lib/libsodium.so jni/armeabi-v7a/

