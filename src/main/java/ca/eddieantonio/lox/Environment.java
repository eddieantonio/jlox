package ca.eddieantonio.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    interface Variable{
        Object get(Token where);
    }

    static class UnboundVariable implements Variable {
        public Object get(Token where) {
            throw new RuntimeError(where, "tried to use unbound variable '" + where.lexeme + "'");
        }
    }

    static class BoundVariable implements Variable {
        final private Object value;

        BoundVariable(Object value) {
            this.value = value;
        }

        public Object get(Token where) {
            return value;
        }
    }

    final Environment enclosing;
    private final Map<String, Variable> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme).get(name);
        }

        if (this.enclosing != null) return this.enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name) {
        values.put(name, new UnboundVariable());
    }

    void define(String name, Object value) {
        values.put(name, new BoundVariable(value));
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, new BoundVariable(value));
            return;
        }

        if (this.enclosing != null) {
            this.enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
