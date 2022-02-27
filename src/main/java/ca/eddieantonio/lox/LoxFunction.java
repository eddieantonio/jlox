package ca.eddieantonio.lox;

import java.util.List;

public class LoxFunction extends UserDefinedFunction {
    private final Stmt.Function declaration;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        super(closure);
        this.declaration = declaration;
    }

    @Override
    protected List<Stmt> body() {
        return declaration.body;
    }

    @Override
    protected List<Token> params() {
        return declaration.params;
    }

    @Override
    public String toString() {
        return "<user-defined fun '" + declaration.name.lexeme + "'>";
    }
}
