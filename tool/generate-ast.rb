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
    "Assign"   => ["Token name", "Expr value"],
    "Binary"   => ["Expr left", "Token operator", "Expr right"],
    "Call"     => ["Expr callee", "Token paren", "List<Expr> arguments"],
    "Grouping" => ["Expr expression"],
    "Literal"  => ["Object value"],
    "Logical"  => ["Expr left", "Token operator", "Expr right"],
    "Unary"    => ["Token operator", "Expr right"],
    "Variable" => ["Token name"],
  }
end

ASTGenerator.define do |generator|
  generator.base_name = "Stmt"
  generator.types ={
    "Block"      => ["List<Stmt> statements"],
    "Var"        => ["Token name", "Expr initializer"],
    "Expression" => ["Expr expression"],
    "If"         => ["Expr condition",
                     "Stmt thenBranch", "Stmt elseBranch"],
    "Print"      => ["Expr expression"],
    "While"      => ["Expr condition", "Stmt body"],
  }
end
