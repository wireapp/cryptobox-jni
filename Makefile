SHELL    := /usr/bin/env bash
OS       := $(shell uname -s | tr '[:upper:]' '[:lower:]')
ARCH     := $(shell uname -m)
ifeq ($(OS), darwin)
LIB_TYPE := dylib
LIB_PATH := DYLD_LIBRARY_PATH
LIB_NAME := -install_name
else
LIB_TYPE := so
LIB_PATH := LD_LIBRARY_PATH
LIB_NAME := -soname
endif

include mk/version.mk

.PHONY: all
all: compile

.PHONY: clean
clean:
	rm -rf build/classes
	rm -f build/lib/libcryptobox-jni.$(LIB_TYPE)

.PHONY: compile
compile: cryptobox compile-native compile-java

.PHONY: compile-native
compile-native:
	$(CC) -std=c99 -g -Wall src/cryptobox-jni.c \
	    -I${JAVA_HOME}/include \
	    -I${JAVA_HOME}/include/$(OS) \
	    -Ibuild/include \
	    -Lbuild/lib \
	    -lsodium \
	    -lcryptobox \
	    -shared \
	    -fPIC \
	    -Wl,$(LIB_NAME),libcryptobox-jni.$(LIB_TYPE) \
	    -o build/lib/libcryptobox-jni.$(LIB_TYPE)

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
	cp build/lib/*.$(LIB_TYPE) dist/lib/
	jar -cvf dist/cryptobox-jni-$(VERSION).jar -C build/classes .
	tar -C dist -czf dist/cryptobox-jni-$(OS)-$(ARCH)-$(VERSION).tar.gz lib javadoc cryptobox-jni-$(VERSION).jar

.PHONY: jar
jar: cryptobox compile-native
	mkdir -p dist build/maven
	cp -r src/ build/maven
	cd build/maven && mvn versions\:set versions\:commit -DnewVersion="$(VERSION)"
	cd build/maven && mvn clean package
	cp build/maven/target/cryptobox-jni-$(VERSION).jar dist/cryptobox-jni-$(VERSION)-$(OS)-$(ARCH).jar

.PHONY: install
install: jar
	mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=dist/cryptobox-jni-$(VERSION)-$(OS)-$(ARCH).jar -Dclassifier=$(OS)-$(ARCH)

#############################################################################
# cryptobox

include mk/cryptobox-src.mk

.PHONY: cryptobox
cryptobox: build/lib/libcryptobox.$(LIB_TYPE) build/include/cbox.h

build/lib/libcryptobox.$(LIB_TYPE): libsodium | build/src/$(CRYPTOBOX)
	mkdir -p build/lib
	cd build/src/$(CRYPTOBOX) && \
		PKG_CONFIG_PATH="$(CURDIR)/build/src/$(LIBSODIUM)/build/lib/pkgconfig:$$PKG_CONFIG_PATH" \
		cargo rustc --lib --release -- \
			-L ../../lib \
			-l sodium \
			-C link_args="-Wl,$(LIB_NAME),libcryptobox.$(LIB_TYPE)"
	cp build/src/$(CRYPTOBOX)/target/release/libcryptobox.$(LIB_TYPE) build/lib/libcryptobox.$(LIB_TYPE)
# OSX name mangling
ifeq ($(OS), darwin)
	install_name_tool -id "@loader_path/libcryptobox.dylib" build/lib/libcryptobox.dylib
endif

build/include/cbox.h: | build/src/$(CRYPTOBOX)
	mkdir -p build/include
	cp build/src/$(CRYPTOBOX)/src/cbox.h build/include/

#############################################################################
# libsodium

include mk/libsodium-src.mk

.PHONY: libsodium
libsodium: build/lib/libsodium.$(LIB_TYPE)

build/lib/libsodium.$(LIB_TYPE): build/src/$(LIBSODIUM)
	mkdir -p build/lib
	cd build/src/$(LIBSODIUM) && \
	./configure --prefix="$(CURDIR)/build/src/$(LIBSODIUM)/build" \
				--disable-soname-versions \
		&& make -j3 && make install
	cp build/src/$(LIBSODIUM)/build/lib/libsodium.$(LIB_TYPE) build/lib/
# OSX name mangling
ifeq ($(OS), darwin)
	install_name_tool -id "@loader_path/libsodium.dylib" build/lib/libsodium.dylib
endif
