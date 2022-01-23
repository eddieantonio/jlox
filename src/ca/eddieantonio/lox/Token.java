package ca.eddieantonio.lox;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
    // Possible extension: add column number, filename.
    // OR! Use offset into a source string, where the source string knows where it comes from.

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return "<" + type + " " + lexeme + " " + literal + ">";
    }
}
