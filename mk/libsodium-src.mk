LIBSODIUM_VERSION := 1.0.18
LIBSODIUM_NAME    := libsodium-$(LIBSODIUM_VERSION)
LIBSODIUM_URL     := https://download.libsodium.org/libsodium/releases/$(LIBSODIUM_NAME)-stable.tar.gz

build/src/$(LIBSODIUM_NAME):
	mkdir -p build/src
	cd build/src && \
	curl -LO $(LIBSODIUM_URL) && \
	tar -xzf $(LIBSODIUM_NAME)-stable.tar.gz --transform 's,libsodium-stable,$(LIBSODIUM_NAME),' && \
	rm $(LIBSODIUM_NAME)-stable.tar.gz
