SOURCE_DIRECTORY = src/main/java
EXPR_JAVA = $(SOURCE_DIRECTORY)/ca/eddieantonio/lox/Expr.java

all: java

java: $(EXPR_JAVA)
	gradle build

run: java
	./jlox

.PHONY: all java run

$(EXPR_JAVA): ./tool/generate-ast.rb
	ruby $^ $(shell dirname $@)
