SHELL   := /usr/bin/env bash

ANDROID_NDK_STANDALONE     ?= $(HOME)/android-ndk-standalone
ANDROID_NDK_STANDALONE_X86 ?= $(HOME)/android-ndk-standalone-x86

include ../mk/version.mk

# pkg-config is invoked by libsodium-sys
# cf. https://github.com/alexcrichton/pkg-config-rs/blob/master/src/lib.rs#L12
export PKG_CONFIG_ALLOW_CROSS=1

all: compile

clean:
	$(ANDROID_NDK_HOME)/ndk-build clean || true

compile: cryptobox
	$(ANDROID_NDK_HOME)/ndk-build
	mkdir -p build/classes
	javac -source 1.6 -target 1.6 -d build/classes ../src/java/org/pkaboo/cryptobox/*.java

doc:
	mkdir -p dist/javadoc
	javadoc -Xdoclint:none -public -d dist/javadoc ../src/java/org/pkaboo/cryptobox/*.java

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

jni/armeabi-v7a/libcryptobox.so: libsodium build/src/$(CRYPTOBOX)
	cd build/src/$(CRYPTOBOX) && \
	cargo rustc --lib --release --target=arm-linux-androideabi -- \
		-L ../../libsodium-android-armv7-a/lib \
		-l sodium \
		-C ar=arm-linux-androideabi-ar \
		-C linker=arm-linux-androideabi-gcc \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/armeabi-v7a
	cp build/src/$(CRYPTOBOX)/target/arm-linux-androideabi/release/libcryptobox.so jni/armeabi-v7a/libcryptobox.so

jni/x86/libcryptobox.so: libsodium build/src/$(CRYPTOBOX)
	cd build/src/$(CRYPTOBOX) && \
	cargo rustc --lib --release --target=i686-linux-android -- \
		-L ../../libsodium-android-x86/lib \
		-l sodium \
		-C ar=i686-linux-android-ar \
		-C linker=i686-linux-android-gcc \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/x86
	cp build/src/$(CRYPTOBOX)/target/i686-linux-android/release/libcryptobox.so jni/x86/libcryptobox.so

jni/include/cbox.h: build/src/$(CRYPTOBOX)
	mkdir -p jni/include
	cp build/src/$(CRYPTOBOX)/cbox.h jni/include/

cryptobox: jni/armeabi-v7a/libcryptobox.so jni/x86/libcryptobox.so jni/include/cbox.h

#############################################################################
# libsodium

include ../mk/libsodium-src.mk

jni/armeabi-v7a/libsodium.so: build/src/$(LIBSODIUM)
	cd build/src/$(LIBSODIUM) && \
	export PATH="${PATH}:${ANDROID_NDK_STANDALONE}/bin" && \
	export CFLAGS="-Os -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -marm -march=armv7-a" && \
	./configure --host=arm-linux-androideabi \
	            --with-sysroot="$(ANDROID_NDK_STANDALONE)/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-armv7-a" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/armeabi-v7a
	cp build/libsodium-android-armv7-a/lib/libsodium.so jni/armeabi-v7a/

jni/x86/libsodium.so: build/src/$(LIBSODIUM)
	cd build/src/$(LIBSODIUM) && \
	export PATH="${PATH}:${ANDROID_NDK_STANDALONE_X86}/bin" && \
	export CFLAGS="-Os -march=i686" && \
	./configure --host=i686-linux-android \
	            --with-sysroot="$(ANDROID_NDK_STANDALONE_X86)/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-x86" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/x86
	cp build/libsodium-android-x86/lib/libsodium.so jni/x86/

libsodium: jni/armeabi-v7a/libsodium.so jni/x86/libsodium.so
