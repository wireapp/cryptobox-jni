CRYPTOBOX_VERSION := v0.8.3
CRYPTOBOX         := cryptobox-$(CRYPTOBOX_VERSION)
CRYPTOBOX_GIT_URL := git@github.com:wireapp/cryptobox-c.git

build/src/$(CRYPTOBOX):
	mkdir -p build/src
	cd build/src && \
	git clone $(CRYPTOBOX_GIT_URL) $(CRYPTOBOX) && \
	cd $(CRYPTOBOX) && \
	git checkout $(CRYPTOBOX_VERSION)
