package ca.eddieantonio.lox;

import java.util.List;

public abstract class UserDefinedFunction implements LoxCallable {
    protected final Environment closure;

    public UserDefinedFunction(Environment closure) {
        this.closure = closure;
    }

    protected abstract List<Stmt> body();
    protected abstract List<Token> params();

    public int arity() {
        return params().size();
    }

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
}
