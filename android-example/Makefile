
clean:
	$(MAKE) -C ../android clean
	$(MAKE) -C tests clean
	ant clean

local.properties:
	android update project -p . --subprojects

init: local.properties

compile: init
	mkdir -p bin/classes
	$(MAKE) -C ../android compile
	mkdir -p libs
	cp -r ../android/libs/* libs/
	javac -target 1.6 -source 1.6 -d bin/classes ../src/java/com/wire/cryptobox/*.java
	ant debug

install: compile
	ant debug install

test: compile
	$(MAKE) -C tests test
