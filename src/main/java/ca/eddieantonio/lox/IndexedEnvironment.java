package ca.eddieantonio.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements a fast, array-backed variable storage.
 */
public class IndexedEnvironment implements IndexedNamespace {
    final IndexedNamespace enclosing;
    private final Map<String, Integer> names = new HashMap<>();
    private final Object[] values;
    private int nameIndex = 0;

    IndexedEnvironment(IndexedNamespace enclosing, int size) {
        this.enclosing = enclosing;
        this.values = new Object[size];
    }

    @Override
    public Object get(Token name) {
        if (containsVariable(name)) {
            return values[names.get(name.lexeme)];
        }

        if (this.enclosing != null) return this.enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    @Override
    public void define(String name, Object value) {
        if (names.containsKey(name)) {
            setByName(name, value);
            return;
        }

        // Add a new name to the array.
        values[nameIndex] = value;
        names.put(name, nameIndex);
        nameIndex++;
    }

    @Override
    public Object getAt(int distance, int index) {
        return ancestor(distance).getByIndex(index);
    }

    @Override
    public IndexedNamespace ancestor(int distance) {
        IndexedNamespace environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.parent();
            // If this assert fails, that means there's a bug in the variable resolution logic.
            assert environment != null;
        }

        return environment;
    }

    @Override
    public IndexedNamespace parent() {
        return this.enclosing;
    }

    @Override
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

    @Override
    public void assignAt(int distance, int index, Object value) {
        ancestor(distance).setByIndex(index, value);
    }

    private boolean containsVariable(Token name) {
        return names.containsKey(name.lexeme);
    }

    void setByName(String name, Object value) {
        setByIndex(names.get(name), value);
    }

    @Override
    public void setByIndex(int index, Object value) {
        values[index] = value;
    }

    @Override
    public Object getByIndex(int index) {
        return values[index];
    }
}
