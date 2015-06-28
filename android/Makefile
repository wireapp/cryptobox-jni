SHELL   := /usr/bin/env bash

ANDROID_NDK_STANDALONE ?= $(HOME)/android-ndk-standalone
ANDROID_ABI            := armeabi-v7a
ANDROID_ARCH           := armv7-a

VERSION := 0.1.0-alpha3

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

dist-libs:
	mkdir -p dist/lib
	cp -r libs/* dist/lib/

dist/cryptobox-jni-$(VERSION).jar: compile
	mkdir -p dist
	jar -cvf dist/cryptobox-jni-$(VERSION).jar -C build/classes .

dist-jar: dist/cryptobox-jni-$(VERSION).jar

dist/cryptobox-android-$(VERSION).tar.gz: dist-libs dist-jar doc
	tar -C dist \
		-czf dist/cryptobox-android-$(VERSION).tar.gz \
		lib javadoc cryptobox-jni-$(VERSION).jar

dist-tar: dist/cryptobox-android-$(VERSION).tar.gz

dist/cryptobox-android-$(VERSION).aar: dist-libs dist-jar
	mkdir -p dist/aar/jni
	cp dist/cryptobox-jni-$(VERSION).jar dist/aar/classes.jar
	cp AndroidManifest.xml dist/aar/
	cp -r dist/lib/* dist/aar/jni/
	cd dist/aar && zip -r ../cryptobox-android-$(VERSION).aar *
	rm -rf dist/aar

dist-aar: dist/cryptobox-android-$(VERSION).aar

dist: dist-tar dist-aar

#############################################################################
# cryptobox

include ../mk/cryptobox-src.mk

jni/libcryptobox.so: libsodium build/src/$(CRYPTOBOX)
	cd build/src/$(CRYPTOBOX) && \
	cargo rustc --lib --release --target=arm-linux-androideabi -- \
		-L ../../libsodium-android-armv7-a/lib \
		-l sodium \
		-C ar=arm-linux-androideabi-ar \
		-C linker=arm-linux-androideabi-gcc
	cp build/src/$(CRYPTOBOX)/target/arm-linux-androideabi/release/libcryptobox.so jni/libcryptobox.so

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
