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

dist/cryptobox-android.aar: dist-libs dist-jar
	mkdir -p dist/aar/jni
	cp dist/cryptobox-jni-$(VERSION).jar dist/aar/classes.jar
	cp AndroidManifest.xml dist/aar/
	cp -r dist/lib/* dist/aar/jni/
	cd dist/aar && zip -r ../cryptobox-android.aar *
	rm -rf dist/aar

dist/cryptobox-android-sources.jar:
	jar -cvf dist/cryptobox-android-sources.jar -C ../src/java .

dist/cryptobox-android-javadoc.jar: doc
	jar -cvf dist/cryptobox-android-javadoc.jar -C dist/javadoc .

.PHONY: dist-aar
dist-aar: dist/cryptobox-android.aar

.PHONY: dist-sources
dist-sources: dist/cryptobox-android-sources.jar

.PHONY: dist-javadoc
dist-javadoc: dist/cryptobox-android-javadoc.jar

.PHONY: dist
dist: compile dist-tar dist-aar dist-sources dist-javadoc

#############################################################################
# cryptobox

include ../mk/cryptobox-src.mk

.PHONY: cryptobox
cryptobox: jni/armeabi-v7a/libcryptobox.so jni/x86/libcryptobox.so jni/x86_64/libcryptobox.so jni/arm64-v8a/libcryptobox.so jni/include/cbox.h

jni/armeabi-v7a/libcryptobox.so: libsodium-armeabi-v7a | build/src/$(CRYPTOBOX_NAME)
	cd build/src/$(CRYPTOBOX_NAME) && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export SODIUM_LIB_DIR=../../libsodium-android-armv7-a/lib && \
	export SODIUM_SHARED=1 && \
	cargo rustc --lib --release --target=armv7-linux-androideabi -- \
		-L ../../libsodium-android-arm-v7a/lib \
		-C ar=arm-linux-androideabi-ar \
		-C linker=armv7a-linux-androideabi16-clang \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/armeabi-v7a
	cp build/src/$(CRYPTOBOX_NAME)/target/armv7-linux-androideabi/release/libcryptobox.so jni/armeabi-v7a/libcryptobox.so

jni/x86/libcryptobox.so: libsodium-x86 | build/src/$(CRYPTOBOX_NAME)
	cd build/src/$(CRYPTOBOX_NAME) && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export SODIUM_LIB_DIR=../../libsodium-x86/lib && \
	export SODIUM_SHARED=1 && \
	cargo rustc --lib --release --target=i686-linux-android -- \
		-L ../../libsodium-android-x86/lib \
		-C ar=i686-linux-android-ar \
		-C linker=i686-linux-android16-clang \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/x86
	cp build/src/$(CRYPTOBOX_NAME)/target/i686-linux-android/release/libcryptobox.so jni/x86/libcryptobox.so

jni/x86_64/libcryptobox.so: libsodium-x86_64 | build/src/$(CRYPTOBOX_NAME)
	cd build/src/$(CRYPTOBOX_NAME) && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export SODIUM_LIB_DIR=../../libsodium-x86_64/lib && \
	export SODIUM_SHARED=1 && \
	cargo rustc -v --lib --release --target=x86_64-linux-android -- \
		-L ../../libsodium-android-x86_64/lib \
		-C ar=x86_64-linux-android-ar \
		-C linker=x86_64-linux-android21-clang \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/x86_64
	cp build/src/$(CRYPTOBOX_NAME)/target/x86_64-linux-android/release/libcryptobox.so jni/x86_64/libcryptobox.so

jni/arm64-v8a/libcryptobox.so: libsodium-aarch64 | build/src/$(CRYPTOBOX_NAME)
	cd build/src/$(CRYPTOBOX_NAME) && \
	export SODIUM_LIB_DIR=../../libsodium-arm64-v8a/lib && \
	export SODIUM_SHARED=1 && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	cargo rustc --lib --release --target=aarch64-linux-android -- \
		-L ../../libsodium-android-armv8-a/lib \
		-C ar=aarch64-linux-android-ar \
		-C linker=aarch64-linux-android21-clang \
		-C link_args="-Wl,-soname,libcryptobox.so"
	mkdir -p jni/arm64-v8a
	cp build/src/$(CRYPTOBOX_NAME)/target/aarch64-linux-android/release/libcryptobox.so jni/arm64-v8a/libcryptobox.so


jni/include/cbox.h: | build/src/$(CRYPTOBOX_NAME)
	mkdir -p jni/include
	cp build/src/$(CRYPTOBOX_NAME)/src/cbox.h jni/include/

#############################################################################
# libsodium

include ../mk/libsodium-src.mk

.PHONY: libsodium-armeabi-v7a
libsodium-armeabi-v7a: jni/armeabi-v7a/libsodium.so

.PHONY: libsodium-aarch64
libsodium-aarch64: jni/arm64-v8a/libsodium.so

.PHONY: libsodium-x86
libsodium-x86: jni/x86/libsodium.so

.PHONY: libsodium-x86_64
libsodium-x86_64: jni/x86_64/libsodium.so

.PHONY: libsodium
libsodium: jni/armeabi-v7a/libsodium.so jni/arm64-v8a/libsodium.so jni/x86/libsodium.so jni/x86_64/libsodium.so

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

jni/x86/libsodium.so: | build/src/$(LIBSODIUM_NAME)
	cd build/src/$(LIBSODIUM_NAME) && \
	export CFLAGS="-Os -march=i686" && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export AR=${TOOLCHAIN}/bin/i686-linux-android-ar && \
	export AS=${TOOLCHAIN}/bin/i686-linux-android-as && \
	export CC=${TOOLCHAIN}/bin/i686-linux-android16-clang && \
	export CXX=${TOOLCHAIN}/bin/i686-linux-android16-clang++ && \
	export LD=${TOOLCHAIN}/bin/i686-linux-android-ld && \
	export RANLIB=${TOOLCHAIN}/bin/i686-linux-android-ranlib && \
	export STRIP=${TOOLCHAIN}/bin/i686-linux-android-strip && \
	./configure --host=i686-linux-android \
	            --with-sysroot="${TOOLCHAIN}/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-x86" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/x86
	cp build/libsodium-android-x86/lib/libsodium.so jni/x86/

jni/x86_64/libsodium.so: | build/src/$(LIBSODIUM_NAME)
	cd build/src/$(LIBSODIUM_NAME) && \
	export CFLAGS="-Os -march=x86-64" && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export AR=${TOOLCHAIN}/bin/x86_64-linux-android-ar && \
	export AS=${TOOLCHAIN}/bin/x86_64-linux-android-as && \
	export CC=${TOOLCHAIN}/bin/x86_64-linux-android21-clang && \
	export CXX=${TOOLCHAIN}/bin/x86_64-linux-android21-clang++ && \
	export LD=${TOOLCHAIN}/bin/x86_64-linux-android-ld && \
	export RANLIB=${TOOLCHAIN}/bin/x86_64-linux-android-ranlib && \
	export STRIP=${TOOLCHAIN}/bin/x86_64-linux-android-strip && \
	./configure --host=x86_64-linux-android \
	            --with-sysroot="${TOOLCHAIN}/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-x86_64" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/x86_64
	cp build/libsodium-android-x86_64/lib/libsodium.so jni/x86_64/

jni/arm64-v8a/libsodium.so: | build/src/$(LIBSODIUM_NAME)
	cd build/src/$(LIBSODIUM_NAME) && \
	export CFLAGS="-Os -march=armv8-a" && \
	export PATH="${PATH}:${TOOLCHAIN}/bin" && \
	export AR=${TOOLCHAIN}/bin/aarch64-linux-android-ar && \
	export AS=${TOOLCHAIN}/bin/aarch64-linux-android-as && \
	export CC=${TOOLCHAIN}/bin/aarch64-linux-android21-clang && \
	export CXX=${TOOLCHAIN}/bin/aarch64-linux-android21-clang++ && \
	export LD=${TOOLCHAIN}/bin/aarch64-linux-android-ld && \
	export RANLIB=${TOOLCHAIN}/bin/aarch64-linux-android-ranlib && \
	export STRIP=${TOOLCHAIN}/bin/aarch64-linux-android-strip && \
	./configure --host=aarch64-linux-android \
	            --with-sysroot="${TOOLCHAIN}/sysroot" \
	            --prefix="$(CURDIR)/build/libsodium-android-armv8-a" \
	            --disable-soname-versions \
	            && \
	make clean && \
	make -j3 && make install
	mkdir -p jni/arm64-v8a
	cp build/libsodium-android-armv8-a/lib/libsodium.so jni/arm64-v8a
