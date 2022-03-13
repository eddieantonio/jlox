package ca.eddieantonio.lox;

public enum FunctionKind {
    FUNCTION("function"),
    METHOD("method"),
    GETTER("getter");

    final String title;
    FunctionKind(String title) {
        this.title = title;
    }

    boolean hasFormalParameters() {
        return this != GETTER;
    }


    @Override
    public String toString() {
        return this.title;
    }
}
