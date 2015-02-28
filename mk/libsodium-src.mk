LIBSODIUM_VERSION := 1.0.2
LIBSODIUM         := libsodium-$(LIBSODIUM_VERSION)
LIBSODIUM_URL     := https://download.libsodium.org/libsodium/releases/$(LIBSODIUM).tar.gz

build/src/$(LIBSODIUM):
	mkdir -p build/src
	cd build/src && \
	wget -O $(LIBSODIUM).tar.gz $(LIBSODIUM_URL) && \
	tar -xzf $(LIBSODIUM).tar.gz && \
	rm $(LIBSODIUM).tar.gz
