LIBSODIUM_VERSION := 1.0.18
LIBSODIUM_NAME    := libsodium-$(LIBSODIUM_VERSION)
LIBSODIUM_URL     := http://download.libsodium.org/libsodium/releases/$(LIBSODIUM_NAME).tar.gz

build/src/$(LIBSODIUM_NAME):
	mkdir -p build/src
	cd build/src && \
	wget -O $(LIBSODIUM_NAME).tar.gz $(LIBSODIUM_URL) && \
	tar -xzf $(LIBSODIUM_NAME).tar.gz && \
	rm $(LIBSODIUM_NAME).tar.gz
