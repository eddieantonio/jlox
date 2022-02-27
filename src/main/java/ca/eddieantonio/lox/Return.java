package ca.eddieantonio.lox;

// TODO: I think this is just a regular Throwable, but the book gives this as RuntimeException :S
public class Return extends RuntimeException {
    final Object value;

    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
