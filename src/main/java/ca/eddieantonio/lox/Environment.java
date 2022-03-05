package ca.eddieantonio.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();


    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (containsVariable(name)) {
            return values.get(name.lexeme);
        }

        if (this.enclosing != null) return this.enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        setByName(name, value);
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
            // If this assert fails, that means there's a bug in the variable resolution logic.
            assert environment != null;
        }

        return environment;
    }

    public void assign(Token name, Object value) {
        if (containsVariable(name)) {
            setByName(name.lexeme, value);
            return;
        }

        if (this.enclosing != null) {
            this.enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).setByName(name.lexeme, value);
    }

    private boolean containsVariable(Token name) {
        return values.containsKey(name.lexeme);
    }

    void setByName(String name, Object value) {
        values.put(name, value);
    }
}
