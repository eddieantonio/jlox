package ca.eddieantonio.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final IndexedNamespace closure;
    private final int numberOfLocals;

    LoxFunction(Stmt.Function declaration, IndexedNamespace closure, int numberOfLocals) {
        this.closure = closure;
        this.declaration = declaration;
        this.numberOfLocals = numberOfLocals;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        assert arguments.size() == arity();
        IndexedEnvironment environment = new IndexedEnvironment(closure, numberOfLocals);
        for (int i = 0; i < arity(); i++) {
            environment.setByIndex(i, arguments.get(i));
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
