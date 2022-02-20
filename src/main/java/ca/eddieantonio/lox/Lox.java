package ca.eddieantonio.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    private static final int EXIT_USAGE = 64;
    private static final int EXIT_ERROR = 65;
    private static final int EXIT_RUNTIME_ERROR = 70;

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static Object lastResult = null;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: jlox [script]");
            System.exit(EXIT_USAGE);
        }  else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    static abstract class ExecutionStyle<T> {
        abstract T useParser(Parser parser);
        abstract void useResult(T result);
        void run(String source) {
            Scanner scanner = new Scanner(source);
            List<Token> tokens = scanner.scanTokens();

            Parser parser = new Parser(tokens);
            T result = useParser(parser);

            // Stop if there were any errors during lexing/parsing.
            if (hadError) return;

            // Yay, interpret it!
            useResult(result);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, StandardCharsets.UTF_8));

        if (hadError) {
            System.exit(EXIT_ERROR);
        }
        if (hadRuntimeError) {
            System.exit(EXIT_RUNTIME_ERROR);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;

            run(line);

            if (lastResult != null) {
                System.out.println(Interpreter.stringify(lastResult));
            }

            // Reset error status for the next line.
            hadError = false;
            hadRuntimeError = false;
            lastResult = null;
        }
    }

    private static void run(String source) {
        ExecutionStyle<List<Stmt>> program = new ExecutionStyle<>() {
            @Override
            public List<Stmt> useParser(Parser parser) {
                return parser.parse();
            }

            @Override
            public void useResult(List <Stmt> statements) {
                interpreter.interpret(statements);
            }
        };

        program.run(source);
    }

    static void setResult(Object result) {
        lastResult = result;
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    /**
     * Report an error.
     * @param line line number the error occurred on.
     * @param where idk lol
     * @param message English message
     */
    static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message
        );
        hadError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage()
                + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
