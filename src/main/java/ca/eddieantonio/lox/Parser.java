package ca.eddieantonio.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static ca.eddieantonio.lox.TokenType.*;

public class Parser {
    private static final int MAXIMUM_ARGUMENTS = 255;
    private static final int MAXIMUM_PARAMETERS = MAXIMUM_ARGUMENTS;

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        assert tokens.get(tokens.size() -1).type == EOF;
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            // try to rescue our way out of this!
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() {
        // TODO[error]: better error message
        consume(LEFT_PAREN, "Expecting '(' after for");

        Stmt initializer;
        if (match(SEMICOLON)) {
            // initializer was omitted
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        // TODO[error]: better error message
        consume(SEMICOLON, "Expected ';' after condition in for");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' to end for condition");

        Stmt body = statement();

        // Desugar the for-loop into a while-loop
        // Work backwards from increment...
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        // To condition...
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        // To initializer... note, this creates a new scope ONLY if an initializer is provided.
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt ifStatement() {
        // TODO[error]: better error message: mismatched delimiter error
        consume(LEFT_PAREN, "Expecting '(' after if");
        Expr condition = expression();
        // TODO[error]: better error message: mismatched delimiter error
        consume(RIGHT_PAREN, "Expecting a ')' but couldn't find one");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        // TODO[error]: better error message.
        consume(SEMICOLON, "Expect ';' after print expressions.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        // TODO[error]: better error message.
        consume(SEMICOLON, "Expect ';' after return.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement() {
        // TODO[error]: better error message
        consume(LEFT_PAREN, "Expected '(' after 'while'");
        Expr condition = expression();
        // TODO[error]: better error message
        consume(RIGHT_PAREN, "Expected ')' after while condition");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private List<Stmt> block() {
        List <Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        // TODO[error]: better error message: mismatched delimiters!
        consume(RIGHT_BRACE, "expected '}' to end block");
        return statements;
    }

    private Stmt classDeclaration() {
        // TODO[error]: better error message
        Token name = consume(IDENTIFIER, "Expected the class name after 'class'");

        // TODO[error]: better error message
        consume(LEFT_BRACE, "Expected an open brace after starting a class");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            String kind = "method";
            if (match(CLASS)) {
                kind = "static method";
            }

            // As an error message researcher, I **KNOW** the parser is capable of going off the rails here!
            methods.add(function(kind));
        }
        // TODO[error]: better error message:
        consume(RIGHT_BRACE, "Expected open brace after starting a class");

        return new Stmt.Class(name, methods);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after print expressions.");
        return new Stmt.Expression(value);
    }

    private Stmt.Function function(String kind) {
        // TODO[error]: better error message:
        Token name = consume(IDENTIFIER, "Expected " + kind + " name");
        consume(LEFT_PAREN, "Expected '(' after " + kind + " name");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= MAXIMUM_PARAMETERS) {
                    error(peek(), "Cannot have more than " + MAXIMUM_PARAMETERS + " parameters");
                }
                // TODO[error]: better error message:
                parameters.add(consume(IDENTIFIER, "Expected parameter name"));
            } while (match(COMMA));
        }

        // TODO[error]: better error message:
        // c.f., Marceau et al. 2011, "Mind Your Language" about parameter vs. argument
        consume(RIGHT_PAREN, "Expected ')' after parameter list");

        // TODO[error]: better error message:
        consume(LEFT_BRACE, "Expected '{' before " + kind + " body");
        List<Stmt> body = block();
        // TODO[error]: note: block() needs extra context to produce a better error message.

        return new Stmt.Function(name, kind.equals("static method"), parameters, body);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get get) {
                // Transform that last field .get into a set:
                return new Expr.Set(get.object, get.name, value);
            }

            // TODO[error]: better error message:
            error(equals, "Invalid assignment target");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            return new Expr.Unary(operator, unary());
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                // TODO[error]: Better error message
                Token name = consume(IDENTIFIER, "Expected property name after the dot");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= MAXIMUM_ARGUMENTS) {
                    error(peek(), "Can't have more than " + MAXIMUM_ARGUMENTS + " arguments");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        // TODO[error]: better error message
        Token paren = consume(RIGHT_PAREN, "Expected ')' to end function call");

        return new Expr.Call(callee, paren, arguments);
    }


    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            // The scanner would have already parsed this literal for us.
            return new Expr.Literal(previous().literal);
        }

        if (match(THIS)) return new Expr.This(previous());

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            // TODO[error]: "high expectations" error messages
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // TODO[error]: this is not a good error message
        throw error(peek(), "Expect expression.");
    }


    // Helpers

    /**
     * Consumes the current token if it matches ANY of the given TokenTypes.
     * @return true if the token is matched and consumed
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * @return true if the current token matches the given type.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * Consumes a token unconditionally.
     * @return the consumed token
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * @return true if current is the end of file.
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * @return the current token, without consuming it.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * @return the previous token in the stream.
     */
    private Token previous() {
        assert current > 0;
        return tokens.get(current - 1);
    }

    // Error handling

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Gobble up tokens until we find a token that starts a statement or top-level declaration.
     */
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }

            advance();
        }
    }
}
