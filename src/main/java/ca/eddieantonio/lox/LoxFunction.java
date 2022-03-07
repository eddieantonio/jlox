package ca.eddieantonio.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final IndexedNamespace closure;

    LoxFunction(Stmt.Function declaration, IndexedNamespace closure) {
        this.closure = closure;
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        assert arguments.size() == arity();
        IndexedEnvironment environment = new IndexedEnvironment(closure, arguments.size());
        for (int i = 0; i < arity(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<user-defined fun '" + declaration.name.lexeme + "'>";
    }
}
