SHELL    := /usr/bin/env bash
OS       := $(shell uname -s | tr '[:upper:]' '[:lower:]')
ARCH     := $(shell uname -m)
ifeq ($(OS), darwin)
LIB_TYPE := dylib
LIB_PATH := DYLD_LIBRARY_PATH
else
LIB_TYPE := so
LIB_PATH := LD_LIBRARY_PATH
endif

VERSION := 0.1.0-alpha3

all: compile

clean:
	rm -rf build/classes
	rm -f build/lib/libcryptobox-jni.$(LIB_TYPE)

compile: cryptobox compile-native compile-java

compile-native:
	$(CC) -std=c99 -g -Wall src/cryptobox-jni.c \
	    -I${JAVA_HOME}/include \
	    -Ibuild/include \
	    -Lbuild/lib \
	    -lsodium \
	    -lcryptobox \
	    -shared \
	    -fPIC \
	    -o build/lib/libcryptobox-jni.$(LIB_TYPE)
# OSX name mangling
ifeq ($(OS), darwin)
	install_name_tool -id "libcryptobox-jni.dylib" build/lib/libcryptobox-jni.dylib
endif

compile-java:
	mkdir -p build/classes
	javac -d build/classes src/java/org/pkaboo/cryptobox/*.java

doc:
	mkdir -p dist/javadoc
	javadoc -public -d dist/javadoc src/java/org/pkaboo/cryptobox/*.java

test:
	make -C android-example/tests test

distclean:
	$(MAKE) -C android distclean
	rm -rf build
	rm -rf dist

dist: compile doc
	$(MAKE) -C android dist
	mkdir -p dist/lib
	cp build/lib/*.$(LIB_TYPE) dist/lib/
	jar -cvf dist/cryptobox-jni-$(VERSION).jar -C build/classes .
	tar -C dist -czf dist/cryptobox-jni-$(OS)-$(ARCH)-$(VERSION).tar.gz lib javadoc cryptobox-jni-$(VERSION).jar

#############################################################################
# cryptobox

include mk/cryptobox-src.mk

build/lib/libcryptobox.$(LIB_TYPE): libsodium build/src/$(CRYPTOBOX)
	mkdir -p build/lib
	cd build/src/$(CRYPTOBOX) && cargo rustc --lib --release -- -L ../../lib -l sodium
	cp build/src/$(CRYPTOBOX)/target/release/libcryptobox.$(LIB_TYPE) build/lib/libcryptobox.$(LIB_TYPE)
# OSX name mangling
ifeq ($(OS), darwin)
	install_name_tool -id "@loader_path/libcryptobox.dylib" build/lib/libcryptobox.dylib
endif

build/include/cbox.h: build/src/$(CRYPTOBOX)
	mkdir -p build/include
	cp build/src/$(CRYPTOBOX)/cbox.h build/include/

cryptobox: build/lib/libcryptobox.$(LIB_TYPE) build/include/cbox.h

#############################################################################
# libsodium

include mk/libsodium-src.mk

build/lib/libsodium.$(LIB_TYPE): build/src/$(LIBSODIUM)
	mkdir -p build/lib
	cd build/src/$(LIBSODIUM) && \
	./configure --prefix="$(CURDIR)/build/src/$(LIBSODIUM)/build" && make -j3 && make install
	cp build/src/$(LIBSODIUM)/build/lib/libsodium.$(LIB_TYPE) build/lib/
# OSX name mangling
ifeq ($(OS), darwin)
	install_name_tool -id "@loader_path/libsodium.dylib" build/lib/libsodium.dylib
endif

libsodium: build/lib/libsodium.$(LIB_TYPE)
