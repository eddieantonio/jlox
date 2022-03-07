package ca.eddieantonio.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements a global namespace: a slow, key-value store of variables.
 */
public class GlobalNamespace implements Namespace {
    private final Map<String, Object> values = new HashMap<>();

    @Override
    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    @Override
    public void define(String name, Object value) {
        values.put(name, value);
    }

    @Override
    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
