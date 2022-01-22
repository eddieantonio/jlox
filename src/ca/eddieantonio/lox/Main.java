package ca.eddieantonio.lox;

public class Main {
    public static void main(String[] args) {
        String salutation = args.length > 0 ? "Hello" : "Hallo";
        String recipient = args.length > 0 ? "world" : "Welt";

        String greeting = salutation + ", " + recipient + "!";
        System.out.println(greeting);
    }
}
