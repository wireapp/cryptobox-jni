SHELL    := /usr/bin/env bash
OS       := $(shell uname -s | tr '[:upper:]' '[:lower:]')
ARCH     := $(shell uname -m)
ifeq ($(OS), darwin)
JAVA_OS          := $(OS)
LIB_PATH         := DYLD_LIBRARY_PATH
LIBCRYPTOBOX_JNI := libcryptobox-jni.dylib
LIBCRYPTOBOX     := libcryptobox.dylib
LIBSODIUM        := libsodium.dylib
OPT_SONAME       := -install_name
else ifneq ($(findstring mingw,$(OS)),)
JAVA_OS          := win32
LIB_PATH         := LD_LIBRARY_PATH
LIBCRYPTOBOX_JNI := cryptobox-jni.dll
LIBCRYPTOBOX     := cryptobox.dll
LIBSODIUM        := libsodium.dll
OPT_SONAME       := -soname
else
JAVA_OS          := $(OS)
LIB_PATH         := LD_LIBRARY_PATH
LIBCRYPTOBOX_JNI := libcryptobox-jni.so
LIBCRYPTOBOX     := libcryptobox.so
LIBSODIUM        := libsodium.so
OPT_SONAME       := -soname
endif

include mk/version.mk

.PHONY: all
all: compile

.PHONY: clean
clean:
	rm -rf build/classes
	rm -f build/lib/$(LIBCRYPTOBOX_JNI)

.PHONY: compile
compile: cryptobox compile-native compile-java

.PHONY: compile-native
compile-native:
	$(CC) -std=c99 -g -Wall src/cryptobox-jni.c \
	    -I"${JAVA_HOME}/include" \
	    -I"${JAVA_HOME}/include/$(JAVA_OS)" \
	    -Ibuild/include \
	    -Lbuild/lib \
	    -lsodium \
	    -lcryptobox \
	    -shared \
	    -fPIC \
	    -Wl,$(OPT_SONAME),$(LIBCRYPTOBOX_JNI) \
	    -o build/lib/$(LIBCRYPTOBOX_JNI)

.PHONY: compile-java
compile-java:
	mkdir -p build/classes
	javac -d build/classes src/java/com/wire/cryptobox/*.java

.PHONY: doc
doc:
	mkdir -p dist/javadoc
	javadoc -Xdoclint:none -public -d dist/javadoc src/java/com/wire/cryptobox/*.java

.PHONY: distclean
distclean:
	rm -rf build
	rm -rf dist

.PHONY: dist
dist: compile doc
	mkdir -p dist/lib
	cp build/lib/$(LIBSODIUM) dist/lib/
	cp build/lib/$(LIBCRYPTOBOX) dist/lib/
	cp build/lib/$(LIBCRYPTOBOX_JNI) dist/lib/
	jar -cvf dist/cryptobox-jni-$(VERSION).jar -C build/classes .
	tar -C dist -czf dist/cryptobox-jni-$(OS)-$(ARCH)-$(VERSION).tar.gz lib javadoc cryptobox-jni-$(VERSION).jar

#############################################################################
# cryptobox

include mk/cryptobox-src.mk

.PHONY: cryptobox
cryptobox: build/lib/$(LIBCRYPTOBOX) build/include/cbox.h

build/lib/$(LIBCRYPTOBOX): libsodium | build/src/$(CRYPTOBOX_NAME)
	mkdir -p build/lib
	cd build/src/$(CRYPTOBOX_NAME) && \
		PKG_CONFIG_PATH="$(CURDIR)/build/src/$(LIBSODIUM_NAME)/build/lib/pkgconfig:$$PKG_CONFIG_PATH" \
		cargo rustc --lib --release -- \
			-L ../../lib \
			-l sodium \
			-C link_args="-Wl,$(OPT_SONAME),$(LIBCRYPTOBOX)"
	cp build/src/$(CRYPTOBOX_NAME)/target/release/$(LIBCRYPTOBOX) build/lib/$(LIBCRYPTOBOX)
# OSX name mangling
ifeq ($(OS), darwin)
	install_name_tool -id "@loader_path/$(LIBCRYPTOBOX)" build/lib/$(LIBCRYPTOBOX)
endif

build/include/cbox.h: | build/src/$(CRYPTOBOX_NAME)
	mkdir -p build/include
	cp build/src/$(CRYPTOBOX_NAME)/src/cbox.h build/include/

#############################################################################
# libsodium

include mk/libsodium-src.mk

.PHONY: libsodium
libsodium: build/lib/$(LIBSODIUM)

build/lib/$(LIBSODIUM): build/src/$(LIBSODIUM_NAME)
	mkdir -p build/lib
	cd build/src/$(LIBSODIUM_NAME) && \
	./configure --prefix="$(CURDIR)/build/src/$(LIBSODIUM_NAME)/build" \
				--disable-soname-versions \
		&& make -j3 && make install
ifneq ($(findstring mingw,$(OS)),)
	cp build/src/$(LIBSODIUM_NAME)/build/bin/$(LIBSODIUM) build/lib/
else
	cp build/src/$(LIBSODIUM_NAME)/build/lib/$(LIBSODIUM) build/lib/
endif
# OSX name mangling
ifeq ($(OS), darwin)
	install_name_tool -id "@loader_path/$(LIBSODIUM)" build/lib/$(LIBSODIUM)
endif
