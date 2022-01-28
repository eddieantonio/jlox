all: java

java:
	gradle build

run: java
	./jlox

.PHONY: all java run
