CRYPTOBOX_VERSION := develop
CRYPTOBOX         := cryptobox-$(CRYPTOBOX_VERSION)
CRYPTOBOX_GIT_URL := git@github.com:shared-secret/cryptobox-c.git

build/src/$(CRYPTOBOX):
	mkdir -p build/src
	cd build/src && \
	git clone $(CRYPTOBOX_GIT_URL) $(CRYPTOBOX) && \
	cd $(CRYPTOBOX) && \
	git checkout $(CRYPTOBOX_VERSION)
