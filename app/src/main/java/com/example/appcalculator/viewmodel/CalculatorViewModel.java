package com.example.appcalculator.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appcalculator.utils.ExpressionEvaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalculatorViewModel extends ViewModel {
    private static final String DEFAULT_RESULT = "0";
    private final MutableLiveData<String> expression = new MutableLiveData<>("");
    private final MutableLiveData<String> result = new MutableLiveData<>(DEFAULT_RESULT);
    private final List<Token> tokens = new ArrayList<>();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    public LiveData<String> getExpression() {
        return expression;
    }

    public LiveData<String> getResult() {
        return result;
    }

    public void onDigit(String digit) {
        appendToken(new Token(digit, digit));
    }

    public void onDecimal() {
        appendToken(new Token(".", "."));
    }

    public void onOperator(String operatorDisplay, String operatorRaw) {
        appendToken(new Token(operatorDisplay, operatorRaw));
    }

    public void onFunction(String displayName, String rawName) {
        appendToken(new Token(displayName + "(", rawName + "("));
    }

    public void onPercent() {
        appendToken(new Token("%", "/100"));
    }

    public void onPi() {
        String piValue = String.format(Locale.US, "%.15f", Math.PI);
        appendToken(new Token("π", piValue));
    }

    public void onBackspace() {
        if (!tokens.isEmpty()) {
            tokens.remove(tokens.size() - 1);
            updateLiveData();
        }
    }

    public void onAllClear() {
        tokens.clear();
        expression.setValue("");
        result.setValue(DEFAULT_RESULT);
    }

    public void onEquals() {
        String rawExpression = buildRawExpression();
        if (rawExpression.isEmpty() || !canEvaluate(rawExpression)) {
            return;
        }
        try {
            BigDecimal evaluated = evaluator.evaluate(rawExpression);
            String formatted = format(evaluated);
            tokens.clear();
            tokens.add(new Token(formatted, formatted));
            expression.setValue(formatted);
            result.setValue(formatted);
        } catch (IllegalArgumentException ex) {
            result.setValue("Error");
        }
    }

    private void appendToken(Token token) {
        tokens.add(token);
        updateLiveData();
    }

    private void updateLiveData() {
        String displayExpression = buildDisplayExpression();
        expression.setValue(displayExpression);
        updateResultPreview();
    }

    private void updateResultPreview() {
        String rawExpression = buildRawExpression();
        if (rawExpression.isEmpty()) {
            result.setValue(DEFAULT_RESULT);
            return;
        }
        if (!canEvaluate(rawExpression)) {
            return;
        }
        try {
            BigDecimal evaluated = evaluator.evaluate(rawExpression);
            result.setValue(format(evaluated));
        } catch (IllegalArgumentException ex) {
            result.setValue("Error");
        }
    }

    private boolean canEvaluate(String rawExpression) {
        if (rawExpression.isEmpty()) {
            return false;
        }
        char last = rawExpression.charAt(rawExpression.length() - 1);
        return last != '+' && last != '-' && last != '*' && last != '/' && last != '^'
                && last != '.' && last != '(';
    }

    private String buildDisplayExpression() {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token.display);
        }
        return builder.toString();
    }

    private String buildRawExpression() {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token.raw);
        }
        return builder.toString();
    }

    private String format(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.toPlainString();
    }

    private static class Token {
        private final String display;
        private final String raw;

        private Token(String display, String raw) {
            this.display = display;
            this.raw = raw;
        }
    }
}