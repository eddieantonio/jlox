package ca.eddieantonio.lox;

import java.util.ArrayList;
import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return params().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        assert arguments.size() == arity();
        for (int i = 0; i < arity(); i++) {
            environment.define(params().get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(body(), environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    protected List<Stmt> body() {
        return declaration.body;
    }

    protected List<Token> params() {
        return declaration.params;
    }

    @Override
    public String toString() {
        return "<user-defined fun '" + declaration.name.lexeme + "'>";
    }
}
