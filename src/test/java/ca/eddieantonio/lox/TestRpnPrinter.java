package ca.eddieantonio.lox;


import org.junit.Test;

import static org.junit.Assert.*;

public class TestRpnPrinter {

    @Test
    public void testLiteral() {
        Expr expr = literal(42);
        assertEquals("42", RpnFormatter.format(expr));
    }

    @Test
    public void testBinary() {
        Expr expr = onePlusTwo();
        assertEquals("1 2 +", RpnFormatter.format(expr));
    }

    @Test
    public void testLongerExpression() {
        Expr expr = multiply(group(onePlusTwo()), group(fourMinusThree()));
        assertEquals("1 2 + 4 3 - *", RpnFormatter.format(expr));
    }

    // Utilities
    Expr onePlusTwo() {
        return new Expr.Binary(literal(1), plus(), literal(2));
    }

    Expr fourMinusThree() {
        return new Expr.Binary(literal(4), minus(), literal(3));
    }

    // Technically lox doesn't have ints, but oh well.
    Expr literal(int number) {
        return new Expr.Literal(number);
    }

    Expr group(Expr expr) {
        return new Expr.Grouping(expr);
    }

    Expr multiply(Expr left, Expr right) {
        return new Expr.Binary(left, star(), right);
    }

    Token minus() {
        return new Token(TokenType.MINUS, "-", null, 1);
    }

    Token plus() {
        return new Token(TokenType.PLUS, "+", null, 1);
    }

    Token star() {
        return new Token(TokenType.STAR, "*", null, 1);
    }
}
