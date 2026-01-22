package com.example.appcalculator.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appcalculator.utils.ExpressionEvaluator;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalculatorViewModel extends AndroidViewModel {
    private static final String DEFAULT_RESULT = "0";
    private static final int HISTORY_LIMIT = 8;
    private static final String PREFS_NAME = "calculator_prefs";
    private static final String KEY_HISTORY = "calculation_history";
    private final MutableLiveData<String> expression = new MutableLiveData<>("");
    private final MutableLiveData<String> result = new MutableLiveData<>(DEFAULT_RESULT);
    private final MutableLiveData<String> history = new MutableLiveData<>("");
    private final List<Token> tokens = new ArrayList<>();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final SharedPreferences sharedPreferences;

    public CalculatorViewModel(Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);
        loadHistory();
    }
    public LiveData<String> getExpression() {
        return expression;
    }

    public LiveData<String> getResult() {
        return result;
    }


    public LiveData<String> getHistory() {
        return history;
    }
    public void onDigit(String digit) {
        appendToken(new Token(digit, digit));
    }

    public void onDecimal() {
        appendToken(new Token(".", "."));
    }

    public void onOperator(String operatorDisplay, String operatorRaw) {
        closeOpenFunctionIfNeeded();
        appendToken(new Token(operatorDisplay, operatorRaw));
    }

    public void onFunction(String displayName, String rawName) {
        appendToken(new Token(displayName + "(", rawName + "("));
    }

    public void onPercent() {
        closeOpenFunctionIfNeeded();
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
        String rawExpression = buildRawExpression(false);
        if (rawExpression.isEmpty() || !canEvaluate(rawExpression)) {
            return;
        }
        try {
            String displayExpression = buildDisplayExpression(false);
            BigDecimal evaluated = evaluator.evaluate(autoCloseExpression(rawExpression));
            String formatted = format(evaluated);
            tokens.clear();
            tokens.add(new Token(formatted, formatted));
            expression.setValue(formatted);
            result.setValue(formatted);
            addHistoryEntry(formatDisplayForHistory(displayExpression), formatted);
        } catch (IllegalArgumentException ex) {
            result.setValue(formatErrorMessage(ex));
        }
    }

    private void appendToken(Token token) {
        tokens.add(token);
        updateLiveData();
    }

    private void updateLiveData() {
        String displayExpression = buildDisplayExpression(false);
        expression.setValue(displayExpression);
        updateResultPreview();
    }

    private void updateResultPreview() {
        String rawExpression = buildRawExpression(false);
        if (rawExpression.isEmpty()) {
            result.setValue(DEFAULT_RESULT);
            return;
        }
        if (!canEvaluate(rawExpression)) {
            return;
        }
        try {
            BigDecimal evaluated = evaluator.evaluate(autoCloseExpression(rawExpression));
            result.setValue(format(evaluated));
        } catch (IllegalArgumentException ex) {
            result.setValue(formatErrorMessage(ex));
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

    private String buildDisplayExpression(boolean autoClose) {
        return buildExpression(true, autoClose);
    }

    private String buildRawExpression(boolean autoClose) {
        return buildExpression(false, autoClose);
    }

    private String buildExpression(boolean display, boolean autoClose) {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(display ? token.display : token.raw);
        }
        String base = builder.toString();
        if (!autoClose) {
            return base;
        }
        return autoCloseExpression(base);
    }

    private String autoCloseExpression(String expression) {
        int balance = countParenthesisBalance(expression);
        if (balance <= 0) {
            return expression;
        }
        StringBuilder builder = new StringBuilder(expression);
        for (int i = 0; i < balance; i++) {
            builder.append(')');
        }
        return builder.toString();
    }

    private void closeOpenFunctionIfNeeded() {
        String rawExpression = buildRawExpression(false);
        if (rawExpression.isEmpty()) {
            return;
        }
        int balance = countParenthesisBalance(rawExpression);
        if (balance <= 0) {
            return;
        }
        char lastChar = rawExpression.charAt(rawExpression.length() - 1);
        if (Character.isDigit(lastChar) || lastChar == ')') {
            tokens.add(new Token(")", ")"));
        }
    }

    private int countParenthesisBalance(String expression) {
        int balance = 0;
        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            if (current == '(') {
                balance++;
            } else if (current == ')') {
                balance = Math.max(0, balance - 1);
            }
        }
        return balance;
    }

    private String format(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.toPlainString();
    }

    private String formatDisplayForHistory(String displayExpression) {
        if (displayExpression == null) {
            return "";
        }
        return autoCloseExpression(displayExpression);
    }

    private void addHistoryEntry(String displayExpression, String evaluated) {
        String entry = displayExpression + " = " + evaluated;
        List<String> entries = loadHistoryEntries();
        entries.add(0, entry);
        if (entries.size() > HISTORY_LIMIT) {
            entries = entries.subList(0, HISTORY_LIMIT);
        }
        saveHistoryEntries(entries);
        updateHistoryLiveData(entries);
    }

    private void loadHistory() {
        updateHistoryLiveData(loadHistoryEntries());
    }

    private List<String> loadHistoryEntries() {
        String stored = sharedPreferences.getString(KEY_HISTORY, "[]");
        List<String> entries = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(stored);
            for (int i = 0; i < array.length(); i++) {
                entries.add(array.getString(i));
            }
        } catch (JSONException ignored) {
            return entries;
        }
        return entries;
    }

    private void saveHistoryEntries(List<String> entries) {
        JSONArray array = new JSONArray();
        for (String entry : entries) {
            array.put(entry);
        }
        sharedPreferences.edit().putString(KEY_HISTORY, array.toString()).apply();
    }

    private void updateHistoryLiveData(List<String> entries) {
        if (entries.isEmpty()) {
            history.setValue("");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            builder.append(entries.get(i));
            if (i < entries.size() - 1) {
                builder.append("\n");
            }
        }
        history.setValue(builder.toString());
    }

    private String formatErrorMessage(IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message == null) {
            return "Lỗi không xác định.";
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("division by zero")) {
            return "Không thể chia cho 0.";
        }
        if (normalized.contains("mismatched parentheses") || normalized.contains("misplaced comma")
                || normalized.contains("invalid expression") || normalized.contains("unexpected character")) {
            return "Biểu thức không hợp lệ.";
        }
        if (normalized.contains("log domain") || normalized.contains("ln domain")
                || normalized.contains("sqrt domain")) {
            return "Giá trị ngoài miền xác định.";
        }
        if (normalized.contains("missing operand")) {
            return "Thiếu toán hạng.";
        }
        return "Lỗi: " + message;
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