#!/usr/bin/env ruby

require_relative './libastgen.rb'

# Generates a Java file of AST classes and its corresponding Visitor pattern
# interface.
#
# The AST classes are defined in @types.

if ARGV.empty?
  STDERR.puts "Usage: generate-ast [output-dir]"
  exit(64)
end

ASTGenerator.define do |generator|
  generator.base_name = "Expr"
  generator.types ={
    "Unary"    => ["Token operator", "Expr right"],
    "Binary"   => ["Expr left", "Token operator", "Expr right"],
    "Grouping" => ["Expr expression"],
    "Literal"  => ["Object value"],
    "Variable" => ["Token name"],
  }
end

ASTGenerator.define do |generator|
  generator.base_name = "Stmt"
  generator.types ={
    "Print"      => ["Expr expression"],
    "Expression" => ["Expr expression"],
    "Var"        => ["Token name", "Expr initializer"],
  }
end
