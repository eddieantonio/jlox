package ca.eddieantonio.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Scope> scopes = new Stack<>();
    // NOTE: this kind of violates the single-responsibility principle
    private FunctionType currentFunction = FunctionType.NONE;

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private static class Scope {
        Map<String, Boolean> bindings = new HashMap<>();
        int currentLocal = 0;

        public boolean containsKey(String key) {
            return bindings.containsKey(key);
        }

        public void put(String key, boolean value) {
            bindings.put(key, value);
        }

        public Boolean get(String key) {
            return bindings.get(key);
        }
    }

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    // Temporary.
    private Scope currentScope() {
        return scopes.peek();
    }

    // Temporary
    private Scope newScope() {
        return new Scope();
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(newScope());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Scope scope = currentScope();
        if (scope.containsKey(name.lexeme)) {
            // TODO[error]: better error message (needs to point at previous definition)
            // TODO[error]: also, make it point out the scope.
            Lox.error(name,
        "Already defined a variable with this name in scope");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        currentScope().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        // Walk up through all the scopes STATICALLY, from the innermost,
        // up to the outermost scope.
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                // tell the interpreter how many scopes it has to walk back up.
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
        // If we can't resolve the variable, it's a global.
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();

        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);

        endScope();
        currentFunction = enclosingFunction;
    }

    private boolean declaredButNotDefined(Token name) {
        return currentScope().get(name.lexeme) == Boolean.FALSE;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        // Nothing to resolve 💁
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && declaredButNotDefined(expr.name)) {
            // TODO[error]: better error message
            Lox.error(expr.name,
                    "Can't read local variable in its own initializer");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // Declare a function's name before resolving its body.
        // This way, a function can call itself.
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            // TODO[error]: better error message. I have absolutely no idea how to make this better:
            Lox.error(stmt.keyword, "Can't return from top-level code");
        }

        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }
}
