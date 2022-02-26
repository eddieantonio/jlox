all: java

java:
	gradle build

run: java
	./jlox

test: java
	./jlox example.lox

.PHONY: all java run test
