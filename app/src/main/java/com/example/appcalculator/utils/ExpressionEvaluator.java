package com.example.appcalculator.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public class ExpressionEvaluator {
    private static final MathContext MATH_CONTEXT = new MathContext(34, RoundingMode.HALF_UP);

    public BigDecimal evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression is empty.");
        }
        List<Token> tokens = tokenize(expression);
        List<Token> rpn = toRpn(tokens);
        return evaluateRpn(rpn);
    }

    private List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        Token previous = null;
        while (index < expression.length()) {
            char current = expression.charAt(index);
            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }
            if (Character.isDigit(current) || current == '.') {
                int start = index;
                boolean seenDot = current == '.';
                index++;
                while (index < expression.length()) {
                    char next = expression.charAt(index);
                    if (Character.isDigit(next)) {
                        index++;
                        continue;
                    }
                    if (next == '.' && !seenDot) {
                        seenDot = true;
                        index++;
                        continue;
                    }
                    break;
                }
                String number = expression.substring(start, index);
                tokens.add(new Token(TokenType.NUMBER, number));
                previous = tokens.get(tokens.size() - 1);
                continue;
            }
            if (Character.isLetter(current)) {
                int start = index;
                index++;
                while (index < expression.length() && Character.isLetter(expression.charAt(index))) {
                    index++;
                }
                String name = expression.substring(start, index).toLowerCase(Locale.ROOT);
                tokens.add(new Token(TokenType.FUNCTION, name));
                previous = tokens.get(tokens.size() - 1);
                continue;
            }
            if (current == '+' || current == '-' || current == '*' || current == '/' || current == '^') {
                String operator = String.valueOf(current);
                if (current == '-' && (previous == null || previous.type == TokenType.OPERATOR
                        || previous.type == TokenType.LEFT_PAREN || previous.type == TokenType.COMMA)) {
                    operator = "u-";
                }
                tokens.add(new Token(TokenType.OPERATOR, operator));
                previous = tokens.get(tokens.size() - 1);
                index++;
                continue;
            }
            if (current == '(') {
                tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                previous = tokens.get(tokens.size() - 1);
                index++;
                continue;
            }
            if (current == ')') {
                tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                previous = tokens.get(tokens.size() - 1);
                index++;
                continue;
            }
            if (current == ',') {
                tokens.add(new Token(TokenType.COMMA, ","));
                previous = tokens.get(tokens.size() - 1);
                index++;
                continue;
            }
            throw new IllegalArgumentException("Unexpected character: " + current);
        }
        return tokens;
    }

    private List<Token> toRpn(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                    output.add(token);
                    break;
                case FUNCTION:
                    stack.push(token);
                    break;
                case COMMA:
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LEFT_PAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Misplaced comma.");
                    }
                    break;
                case OPERATOR:
                    while (!stack.isEmpty() && stack.peek().type == TokenType.OPERATOR) {
                        Token top = stack.peek();
                        if ((isLeftAssociative(token) && precedence(token) <= precedence(top))
                                || (!isLeftAssociative(token) && precedence(token) < precedence(top))) {
                            output.add(stack.pop());
                        } else {
                            break;
                        }
                    }
                    stack.push(token);
                    break;
                case LEFT_PAREN:
                    stack.push(token);
                    break;
                case RIGHT_PAREN:
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LEFT_PAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Mismatched parentheses.");
                    }
                    stack.pop();
                    if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION) {
                        output.add(stack.pop());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected token: " + token.type);
            }
        }
        while (!stack.isEmpty()) {
            Token top = stack.pop();
            if (top.type == TokenType.LEFT_PAREN || top.type == TokenType.RIGHT_PAREN) {
                throw new IllegalArgumentException("Mismatched parentheses.");
            }
            output.add(top);
        }
        return output;
    }

    private BigDecimal evaluateRpn(List<Token> tokens) {
        Deque<BigDecimal> stack = new ArrayDeque<>();
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                    stack.push(new BigDecimal(token.value, MATH_CONTEXT));
                    break;
                case OPERATOR:
                    applyOperator(stack, token.value);
                    break;
                case FUNCTION:
                    applyFunction(stack, token.value);
                    break;
                default:
                    throw new IllegalStateException("Unexpected token: " + token.type);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression.");
        }
        return stack.pop().stripTrailingZeros();
    }

    private void applyOperator(Deque<BigDecimal> stack, String operator) {
        if ("u-".equals(operator)) {
            BigDecimal value = popRequired(stack, operator);
            stack.push(value.negate(MATH_CONTEXT));
            return;
        }
        BigDecimal right = popRequired(stack, operator);
        BigDecimal left = popRequired(stack, operator);
        switch (operator) {
            case "+":
                stack.push(left.add(right, MATH_CONTEXT));
                break;
            case "-":
                stack.push(left.subtract(right, MATH_CONTEXT));
                break;
            case "*":
                stack.push(left.multiply(right, MATH_CONTEXT));
                break;
            case "/":
                if (right.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalArgumentException("Division by zero.");
                }
                stack.push(left.divide(right, MATH_CONTEXT));
                break;
            case "^":
                stack.push(fromDouble(Math.pow(left.doubleValue(), right.doubleValue())));
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    private void applyFunction(Deque<BigDecimal> stack, String function) {
        switch (function) {
            case "sin":
                stack.push(fromDouble(Math.sin(popRequired(stack, function).doubleValue())));
                break;
            case "cos":
                stack.push(fromDouble(Math.cos(popRequired(stack, function).doubleValue())));
                break;
            case "tan":
                stack.push(fromDouble(Math.tan(popRequired(stack, function).doubleValue())));
                break;
            case "cot":
                double tanValue = Math.tan(popRequired(stack, function).doubleValue());
                if (tanValue == 0) {
                    throw new IllegalArgumentException("Division by zero.");
                }
                stack.push(fromDouble(1 / tanValue));
                break;
            case "log":
                BigDecimal logValue = popRequired(stack, function);
                if (logValue.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Log domain error.");
                }
                stack.push(fromDouble(Math.log10(logValue.doubleValue())));
                break;
            case "ln":
                BigDecimal lnValue = popRequired(stack, function);
                if (lnValue.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Ln domain error.");
                }
                stack.push(fromDouble(Math.log(lnValue.doubleValue())));
                break;
            case "sqrt":
                BigDecimal sqrtValue = popRequired(stack, function);
                if (sqrtValue.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Sqrt domain error.");
                }
                stack.push(fromDouble(Math.sqrt(sqrtValue.doubleValue())));
                break;
            case "pow":
                BigDecimal exponent = popRequired(stack, function);
                BigDecimal base = popRequired(stack, function);
                stack.push(fromDouble(Math.pow(base.doubleValue(), exponent.doubleValue())));
                break;
            default:
                throw new IllegalArgumentException("Unknown function: " + function);
        }
    }

    private BigDecimal popRequired(Deque<BigDecimal> stack, String context) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Missing operand for " + context);
        }
        return stack.pop();
    }

    private BigDecimal fromDouble(double value) {
        return new BigDecimal(Double.toString(value), MATH_CONTEXT);
    }

    private int precedence(Token token) {
        switch (token.value) {
            case "u-":
                return 4;
            case "^":
                return 3;
            case "*":
            case "/":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                throw new IllegalArgumentException("Unknown operator: " + token.value);
        }
    }

    private boolean isLeftAssociative(Token token) {
        return !"^".equals(token.value) && !"u-".equals(token.value);
    }

    private enum TokenType {
        NUMBER,
        OPERATOR,
        FUNCTION,
        LEFT_PAREN,
        RIGHT_PAREN,
        COMMA
    }

    private static class Token {
        private final TokenType type;
        private final String value;

        private Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}