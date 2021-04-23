package mainPackage;

import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static final boolean in(char char_, String str) {

        for (int charIndex = -1; ++charIndex < str.length();) {
            char currentChar = str.charAt(charIndex);

            if (char_ == currentChar)
                return true;
            // else
            //     continue;
        }
        return false;
    }

    public static final boolean __in(String chars, String str) {

        for (int charsIndex = -1; ++charsIndex < chars.length();) {
            char currentCompareChar = chars.charAt(charsIndex);

            for (int charIndex = -1; ++charIndex < str.length();) {
                char currentChar = str.charAt(charIndex);

                if (currentCompareChar == currentChar)
                    return true;
                // else
                //     continue;
            }
        }
        return false;
    }
    
    public static final class Token {
        
        private final String type ;
        private final String value;
    
        public Token(String value, String type) {
            this.type  = type ;
            this.value = value;
        }

        public final boolean matchType(String type) 
            { return this.type == type; }

        public final String  toString() 
            { return "(\'" + this.type + "\', \'" + this.value + "\')"; }
    }

    public static final class Lexer {
        private String expression;

        public Lexer(String expression)
            { this.expression = expression; }

        public final char[] clean(String str) {
            char[] chars     = new char[str.length()];
            int    revolving = 0;

            for (int charIndex = -1; ++charIndex < str.length();) {
                char currentChar = str.charAt(charIndex);

                if (in(currentChar, " \n")) {
                    ++revolving;
                    continue;
                }
                chars[charIndex - revolving] = currentChar;
            }

            return chars;
        }

        public final ArrayList<Token> lex() {

            final ArrayList<Token> tokens = new ArrayList();
                  char[] _cleanExpression = clean(this.expression);


            final String alphabetic = "abcdefghijklmnopqrstuvwxyz\'";
            final String numeric    = "1234567890.";
                  String generic    = "";
            
            for (char currentToken : _cleanExpression) {

                // Generics
                if (in(currentToken, alphabetic + numeric)) {
                    generic += currentToken;
                    continue;
                }         
                else if (generic != "") {

                    if (__in(alphabetic, generic)) {
                        tokens.add(new Token(generic, "TID"));
                    } else {
                        if (in('.', generic))
                            tokens.add(new Token(generic, "FLT"));
                        else
                            tokens.add(new Token(generic, "INT"));
                    }
                    
                    generic = "";
                }

                // Symbols
                switch (currentToken) {

                    // Miscelaneous
                    case '(':
                        tokens.add(new Token("(", "LPR"));
                        break;
                    case ')':
                        tokens.add(new Token(")", "RPR"));
                        break;
                    case '\\':
                        tokens.add(new Token("\\", "MKV"));
                        break;
                    case ':':
                        tokens.add(new Token(":", "EQS"));
                        break;
                    case ';':
                        tokens.add(new Token(";", "NXT"));
                    break;

                    // Arithmetics
                    case '^':
                        tokens.add(new Token("^", "POW"));
                        break;
                    case '~':
                        tokens.add(new Token("~", "SQR"));
                        break;
                    case '*':
                        tokens.add(new Token("*", "MUL"));
                        break;
                    case '/':
                        tokens.add(new Token("/", "DIV"));
                        break;
                    case '+':
                        tokens.add(new Token("+", "ADD"));
                        break;
                    case '-':
                        tokens.add(new Token("-", "SUB"));
                        break;

                    default:
                        continue;
                }
            }

            return tokens;
        }
    }

    public static final class IntNode {
        private final int value;
        
        public IntNode(int value) {
            this.value = value;
        }

        public IntNode ADD(IntNode other) {
            return new IntNode(this.value + other.value);
        }

        public final String toString() {
            return "int::\'" + this.value + "\'";
        }
    }

    public static final class FloatNode {
        private final float value;
        
        public FloatNode(float value) {
            this.value = value;
        }

        public final String toString() {
            return "float::\'" + this.value + "\'";
        }
    }

    public static final class Node<NodeType> {
        private final NodeType innerNode;
        
        public Node(NodeType innerNode) {
            this.innerNode = innerNode;
        }
        
        public final String toString() {
            return "[" + this.innerNode.toString() + "]";
        }
    }

    public static final class BinaryOperationNode {
        private final Token operationToken;
        private final Node  leftNode;
        private final Node  rightNode;

        public BinaryOperationNode(Token operationToken, Node leftNode, Node rightNode) {
            this.operationToken = operationToken;
            this.leftNode       = leftNode;
            this.rightNode      = rightNode;
        }

        public final String toString() {
            return "("+ this.operationToken.type +", "+ this.leftNode +", "+ this.rightNode +")";
        }
    }

    public static final class UnaryOperationNode {
        private final Token operationToken;
        private final Node  innerNode;

        public UnaryOperationNode(Token operationToken, Node innerNode) {
            this.operationToken = operationToken;
            this.innerNode = innerNode;
        }

        public final String toString() {
            return "("+ this.operationToken.type +", "+ this.innerNode +")";
        }
    }

    public static final class Parser {
        private final ArrayList<Token> tokens;
        private       Token            currentToken;
        private       int              tokenIndex;

        public Parser(ArrayList<Token> tokens) { 
            this.tokens     = tokens;
            this.tokenIndex = -1; 

            this.advance();
        }

        private final void advance() {
            this.tokenIndex++;

            if (this.tokenIndex < this.tokens.size())
                this.currentToken = this.tokens.get(tokenIndex);
        }

        private final Node parse() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            return this.expression();
        }

        private final Node binaryOperation (Method leftNodeType, String operations, Method rightNodeType) throws IllegalAccessException, InvocationTargetException {
            
            Node leftNode = (Node) leftNodeType.invoke(this);

            while (operations.contains(this.currentToken.type)) {
                Token operationToken = this.currentToken;

                this.advance();

                Node rightNode = (Node) rightNodeType.invoke(this);

                leftNode = new Node<BinaryOperationNode>(new BinaryOperationNode(
                    operationToken, leftNode, rightNode
                ));
            }

            return leftNode;
        }

        private final Node expression() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            return this.binaryOperation(
                Parser.class.getDeclaredMethod("term"), 
                "ADD|SUB", 
                Parser.class.getDeclaredMethod("term")
            );
        }

        private final Node term() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            return this.binaryOperation(
                Parser.class.getDeclaredMethod("atom"), 
                "MUL|DIV", 
                Parser.class.getDeclaredMethod("atom")
            );
        }

        private final Node atom() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Token token = this.currentToken;

            switch (token.type) {
                case "INT": this.advance();
                    return new Node<IntNode>(new IntNode(Integer.parseInt(token.value)));

                case "FLT": this.advance();
                    return new Node<FloatNode>(new FloatNode(Float.parseFloat(token.value)));

                case "ADD":
                case "SUB": this.advance();
                    return new Node<UnaryOperationNode>(new UnaryOperationNode(token, this.atom()));

                default
                    :break;
            }

            // FIX

            // if (token.type == "LPR") {
            //     Node expression = (Node) this.expression();

            //     System.out.println(expression); return null;

            //     // if (this.currentToken.type == "RPR") {
            //     //     return expression;
            //     // }
            // }

            return null;
        }
    }

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String           expression = "1 + 2 * 3";
        ArrayList<Token> tokens     = (new Lexer(expression)).lex();

        for (Token token : tokens) System.out.printf("%s\n", token);

        System.out.println(new Parser(tokens).parse());

        System.out.println("");
    }
}
