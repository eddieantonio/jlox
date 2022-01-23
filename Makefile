./src/ca/eddieantonio/lox/Expr.java: ./tool/generate-ast.rb
	ruby $^ $(shell dirname $@)
