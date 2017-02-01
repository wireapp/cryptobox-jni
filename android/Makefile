SHELL   := /usr/bin/env bash

ANDROID_NDK_TOOLCHAIN_ARM     ?=
ANDROID_NDK_TOOLCHAIN_X86     ?=
ANDROID_NDK_TOOLCHAIN_AARCH64 ?=

include ../mk/version.mk

# pkg-config is invoked by libsodium-sys
# cf. https://github.com/alexcrichton/pkg-config-rs/blob/master/src/lib.rs#L12
export PKG_CONFIG_ALLOW_CROSS=1

.PHONY: all
all: compile

.PHONY: clean
clean:
	$(ANDROID_NDK_HOME)/ndk-build clean || true

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
dist: dist-tar dist-aar

#############################################################################
# cryptobox

include ../mk/cryptobox-src.mk

.PHONY: cryptobox
cryptobox: jni/armeabi-v7a/libcryptobox.so jni/x86/libcryptobox.so jni/include/cbox.h

jni/armeabi-v7a/libcryptobox.so: libsodium | build/src/$(CRYPTOBOX_NAME)
ifndef ANDROID_NDK_TOOLCHAIN_ARM
$(error ANDROID_NDK_TOOLCHAIN_ARM is not set)
endif
	cd build/src/$(CRYPTOBOX_NAME) && \
	export PATH="${PATH}:${ANDROID_NDK_TOOLCHAIN_ARM}/bin" && \
	cargo rustc --lib --release --target=armv7-linux-androideabi -- \
		-L ../../libsodium-android-armv7-a/lib \
		-l sodium \
		-C ar=arm-linux-androideabi-ar \
		-C linker=arm-linux-androideabi-gcc \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/armeabi-v7a
	cp build/src/$(CRYPTOBOX_NAME)/target/armv7-linux-androideabi/release/libcryptobox.so jni/armeabi-v7a/libcryptobox.so

jni/x86/libcryptobox.so: libsodium | build/src/$(CRYPTOBOX_NAME)
ifndef ANDROID_NDK_TOOLCHAIN_X86
$(error ANDROID_NDK_TOOLCHAIN_X86 is not set)
endif
	cd build/src/$(CRYPTOBOX_NAME) && \
	export PATH="${PATH}:${ANDROID_NDK_TOOLCHAIN_X86}/bin" && \
	cargo rustc --lib --release --target=i686-linux-android -- \
		-L ../../libsodium-android-x86/lib \
		-l sodium \
		-C ar=i686-linux-android-ar \
		-C linker=i686-linux-android-gcc \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/x86
	cp build/src/$(CRYPTOBOX_NAME)/target/i686-linux-android/release/libcryptobox.so jni/x86/libcryptobox.so

jni/arm64-v8a/libcryptobox.so: libsodium | build/src/$(CRYPTOBOX_NAME)
ifndef ANDROID_NDK_TOOLCHAIN_AARCH64
$(error ANDROID_NDK_TOOLCHAIN_AARCH64 is not set)
endif
	cd build/src/$(CRYPTOBOX_NAME) && \
	export PATH="${PATH}:${ANDROID_NDK_TOOLCHAIN_AARCH64}/bin" && \
	cargo rustc --lib --release --target=aarch64-linux-android -- \
		-L ../../libsodium-android-armv8-a/lib \
		-l sodium \
		-C ar=aarch64-linux-android-ar \
		-C linker=aarch64-linux-android-gcc \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/arm64-v8a
	cp build/src/$(CRYPTOBOX_NAME)/target/aarch64-linux-android/release/libcryptobox.so jni/arm64-v8a/libcryptobox.so

jni/include/cbox.h: | build/src/$(CRYPTOBOX_NAME)
	mkdir -p jni/include
	cp build/src/$(CRYPTOBOX_NAME)/src/cbox.h jni/include/

#############################################################################
# libsodium

include ../mk/libsodium-src.mk

.PHONY: libsodium
libsodium: jni/armeabi-v7a/libsodium.so jni/x86/libsodium.so

jni/armeabi-v7a/libsodium.so: | build/src/$(LIBSODIUM_NAME)
ifndef ANDROID_NDK_TOOLCHAIN_ARM
$(error ANDROID_NDK_TOOLCHAIN_ARM is not set)
endif
	cd build/src/$(LIBSODIUM_NAME) && \
	export CFLAGS="-Os -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -marm -march=armv7-a" && \
	export PATH="${PATH}:${ANDROID_NDK_TOOLCHAIN_ARM}/bin:" && \
	./configure --host=arm-linux-androideabi \
	            --with-sysroot="$(ANDROID_NDK_TOOLCHAIN_ARM)/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-armv7-a" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/armeabi-v7a
	cp build/libsodium-android-armv7-a/lib/libsodium.so jni/armeabi-v7a/

jni/x86/libsodium.so: | build/src/$(LIBSODIUM_NAME)
ifndef ANDROID_NDK_TOOLCHAIN_X86
$(error ANDROID_NDK_TOOLCHAIN_X86 is not set)
endif
	cd build/src/$(LIBSODIUM_NAME) && \
	export CFLAGS="-Os -march=i686" && \
	export PATH="${PATH}:${ANDROID_NDK_TOOLCHAIN_X86}/bin" && \
	./configure --host=i686-linux-android \
	            --with-sysroot="$(ANDROID_NDK_TOOLCHAIN_X86)/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-x86" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/x86
	cp build/libsodium-android-x86/lib/libsodium.so jni/x86/

jni/arm64-v8a/libsodium.so: | build/src/$(LIBSODIUM_NAME)
ifndef ANDROID_NDK_TOOLCHAIN_AARCH64
$(error ANDROID_NDK_TOOLCHAIN_AARCH64 is not set)
endif
	cd build/src/$(LIBSODIUM_NAME) && \
	export CFLAGS="-Os -march=armv8-a" && \
	export PATH="${PATH}:${ANDROID_NDK_TOOLCHAIN_AARCH64}/bin" && \
	./configure --host=aarch64-linux-android \
	            --with-sysroot="$(ANDROID_NDK_TOOLCHAIN_AARCH64)/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-armv8-a" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/arm64-v8a
	cp build/libsodium-android-armv8-a/lib/libsodium.so jni/arm64-v8a/
