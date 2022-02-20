package ca.eddieantonio.lox;

import java.util.ArrayList;
import java.util.List;
import static ca.eddieantonio.lox.TokenType.*;

public class Parser {
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
            statements.add(statement());
        }

        return statements;
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        // TODO[error]: better error message.
        consume(SEMICOLON, "Expect ';' after print expressions.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after print expressions.");
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        return equality();
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

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            // The scanner would have already parsed this literal for us.
            return new Expr.Literal(previous().literal);
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
