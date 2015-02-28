SHELL   := /usr/bin/env bash

VERSION := 0.1.0-alpha

ANDROID_NDK_STANDALONE ?= $(HOME)/android-ndk-standalone
ANDROID_ABI            := armeabi-v7a
ANDROID_ARCH           := armv7-a

all: compile

clean:
	$(ANDROID_NDK_HOME)/ndk-build clean || true

compile: cryptobox
	$(ANDROID_NDK_HOME)/ndk-build
	mkdir -p build/classes
	javac -source 1.6 -target 1.6 -d build/classes ../src/java/org/pkaboo/cryptobox/*.java

doc:
	mkdir -p dist/javadoc
	javadoc -public -d dist/javadoc ../src/java/org/pkaboo/cryptobox/*.java

distclean:
	rm -rf build
	rm -rf dist
	rm -f jni/*.so

dist: compile doc
	mkdir -p dist/lib
	cp libs/$(ANDROID_ABI)/*.so dist/lib/
	jar -cvf dist/cryptobox-jni-$(VERSION).jar -C build/classes .
	tar -C dist -czf dist/cryptobox-android-$(VERSION).tar.gz lib javadoc cryptobox-jni-$(VERSION).jar

#############################################################################
# cryptobox

include ../mk/cryptobox-src.mk

jni/libcryptobox.so: libsodium build/src/$(CRYPTOBOX)
	cd build/src/$(CRYPTOBOX) && \
	cargo build --release --target=arm-linux-androideabi
	cp build/src/$(CRYPTOBOX)/target/arm-linux-androideabi/release/libcryptobox-*.so jni/libcryptobox.so

jni/include/cbox.h: build/src/$(CRYPTOBOX)
	mkdir -p jni/include
	cp build/src/$(CRYPTOBOX)/cbox.h jni/include/

cryptobox: jni/libcryptobox.so jni/include/cbox.h

#############################################################################
# libsodium

include ../mk/libsodium-src.mk

jni/libsodium.so: build/src/$(LIBSODIUM)
	cd build/src/$(LIBSODIUM) && \
	export CFLAGS="-Os -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -marm -march=$(ANDROID_ARCH)" && \
	./configure --host=arm-linux-androideabi \
	            --with-sysroot="$(ANDROID_NDK_STANDALONE)/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-$(ANDROID_ARCH)" \
	            --disable-soname-versions \
	            && \
	make -j3 && make install
	cp build/libsodium-android-$(ANDROID_ARCH)/lib/libsodium.so jni/

libsodium: jni/libsodium.so
