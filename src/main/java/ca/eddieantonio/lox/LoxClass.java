package ca.eddieantonio.lox;

public interface LoxClass {
    String name();
    LoxFunction findMethod(String name);
}
