package ca.eddieantonio.lox;

public interface Namespace {
    /**
     * Get the value of a variable in this namespace, or any parent namespaces.
     */
    Object get(Token name);

    /**
     * Define a variable in this particular namespace.
     */
    void define(String name, Object value);

    /**
     * Assign to the variable in the closest namespace.
     */
    void assign(Token name, Object value);
}
