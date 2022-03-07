package ca.eddieantonio.lox;

/**
 * Implements a fast, array-backed variable storage.
 */
public class IndexedEnvironment implements IndexedNamespace {
    final IndexedNamespace enclosing;
    private final Object[] values;

    IndexedEnvironment(IndexedNamespace enclosing, int size) {
        this.enclosing = enclosing;
        this.values = new Object[size];
    }

    @Override
    public Object get(Token name) {
        throw new AssertionError("Tried to get local variable by name: " + name.lexeme);
    }

    @Override
    public void define(String name, Object value) {
        throw new AssertionError("Should never attempt to define an indexed local variable.");
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
        throw new AssertionError("Tried to assign local variable by name: " + name.lexeme);
    }

    @Override
    public void assignAt(int distance, int index, Object value) {
        ancestor(distance).setByIndex(index, value);
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
