package ca.eddieantonio.lox;

public interface IndexedNamespace extends Namespace {
    /**
     * Get an ancestor of this namespace.
     */
    IndexedNamespace ancestor(int distance);

    /**
     * @return The immediate parent of this namespace.
     */
    IndexedNamespace parent();

    /**
     * Get a value by the given index.
     */
    Object getByIndex(int index);

    /**
     * Get a value by the given index.
     */
    void setByIndex(int index, Object value);

    /**
     * Get a value at the given index, following chain of ancestors.
     */
    Object getAt(int distance, int index);

    /**
     * Assign a variable at the given index, following chain of ancestors.
     */
    void assignAt(int distance, int index, Object value);
}
