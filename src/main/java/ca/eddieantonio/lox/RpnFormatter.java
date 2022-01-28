package ca.eddieantonio.lox;

public class RpnFormatter implements Expr.Visitor<String> {
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        String left = expr.left.accept(this);
        String right = expr.right.accept(this);

        return left + " " + right + " " + expr.operator.lexeme;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        // The magic of RPN is that there are no parens.
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        throw new UnsupportedOperationException();
    }

    public static String format(Expr expr) {
        return expr.accept(new RpnFormatter());
    }
}
