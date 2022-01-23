package ca.eddieantonio.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static ca.eddieantonio.lox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    // Offset into the source where the current token starts
    private int start = 0;
    // Offset into the source where we are currently scanning
    private int current = 0;
    // Line number, as understood by the user.
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        // Always append EOF token.
        tokens.add(new Token(EOF, "", null, line));

        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            // Handle easy tokens first.
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            // Tokens that require lookahead
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('>') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // Handle single-line comment (consumes and discards input until end of the line).
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;

            case '\n':
                line++;
                break;

            // Literals. These require some work...
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isIDStart(c)) {
                    identifier();
                } else {
                    // TODO: [Error] indicate which character is unexpected and offer a solution.
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;

        }

        boolean matchedClosingQuote = match('"');
        assert(matchedClosingQuote);

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

     private void number() {
        while (isDigit(peek()))
            advance();

        // Look for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
     }

     private void identifier() {
        while (isIDContinue(peek()))
            advance();

        // Figure out if it's a keyword:
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            // Not a keyword, just a regular identifier.
            type = IDENTIFIER;

        addToken(type);
     }

    /**
     * Lookahead at the next character in the input.
     * Checks whether the next character in the source is equal to the argument.
     * Consumes input IF AND ONLY IF the argument does indeed match.
     * @return true when the character matches AND consumed.
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    /**
     * Lookahead at the next character in the input.
     * Does NOT consume input.
     * Returns '\0' if at end of the source.
     *
     * @return the next character in the input, without consuming input.
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Lookahead two characters in advance.
     */
    private char peekNext() {
        // Don't go past the end of the source code.
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isIDStart(char c) {
        return (c >= 'A' && c <= 'Z') ||
               (c >= 'a' && c <= 'z') ||
                c == '_';
    }

    private boolean isIDContinue(char c) {
        return isIDStart(c) || isDigit(c);
    }

    /** @return the next character in the input, consuming it. */
    private char advance() {
        return source.charAt(current++);
    }

    /** Appends a simple token to the token stream */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /** Appends a token to the token stream. The token may have a literal value. */
    private void addToken(TokenType type, Object literal) {
        // N.B., (start, current) are all that are need for the token class to
        // track position in a file; the rest can be inferred by keeping a reference to the source file.
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
