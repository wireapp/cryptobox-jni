CRYPTOBOX_VERSION := v1.1.2
CRYPTOBOX_NAME    := cryptobox-$(CRYPTOBOX_VERSION)
CRYPTOBOX_GIT_URL := https://github.com/wireapp/cryptobox-c.git

build/src/$(CRYPTOBOX_NAME):
	mkdir -p build/src
	cd build/src && \
	git clone $(CRYPTOBOX_GIT_URL) $(CRYPTOBOX_NAME) && \
	cd $(CRYPTOBOX_NAME) && \
	git checkout $(CRYPTOBOX_VERSION)
