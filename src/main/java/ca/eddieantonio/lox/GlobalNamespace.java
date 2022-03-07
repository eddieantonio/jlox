package ca.eddieantonio.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalNamespace implements Namespace {
    final Environment enclosing;
    private final Map<String, Integer> names = new HashMap<>();
    private final List<Object> values = new ArrayList<>();


    GlobalNamespace() {
        enclosing = null;
    }

    GlobalNamespace(Environment enclosing) {
        this.enclosing = enclosing;
    }

    @Override
    public Object get(Token name) {
        if (containsVariable(name)) {
            return values.get(names.get(name.lexeme));
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
        int index = values.size();
        values.add(value);
        names.put(name, index);
    }

    public Object getAt(int distance, int index) {
        return ancestor(distance).getByIndex(index);
    }

    public Environment ancestor(int distance) {
        throw new UnsupportedOperationException();
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

    public void assignAt(int distance, int index, Object value) {
        ancestor(distance).setByIndex(index, value);
    }

    private boolean containsVariable(Token name) {
        return names.containsKey(name.lexeme);
    }

    void setByName(String name, Object value) {
        setByIndex(names.get(name), value);
    }

    void setByIndex(int index, Object value) {
        values.set(index, value);
    }

    Object getByIndex(int index) {
        return values.get(index);
    }
}
