package com.example.appcalculator;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.appcalculator.viewmodel.CalculatorViewModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CalculatorViewModel viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);

        TextView expressionView = findViewById(R.id.text_expression);
        TextView resultView = findViewById(R.id.text_result);
        TextView historyView = findViewById(R.id.text_history);

        viewModel.getExpression().observe(this, expressionView::setText);
        viewModel.getResult().observe(this, resultView::setText);
        viewModel.getHistory().observe(this, historyView::setText);

        bindDigitButton(viewModel, R.id.button_0, "0");
        bindDigitButton(viewModel, R.id.button_1, "1");
        bindDigitButton(viewModel, R.id.button_2, "2");
        bindDigitButton(viewModel, R.id.button_3, "3");
        bindDigitButton(viewModel, R.id.button_4, "4");
        bindDigitButton(viewModel, R.id.button_5, "5");
        bindDigitButton(viewModel, R.id.button_6, "6");
        bindDigitButton(viewModel, R.id.button_7, "7");
        bindDigitButton(viewModel, R.id.button_8, "8");
        bindDigitButton(viewModel, R.id.button_9, "9");

        bindButton(R.id.button_decimal, () -> viewModel.onDecimal());
        bindButton(R.id.button_plus, () -> viewModel.onOperator("+", "+"));
        bindButton(R.id.button_minus, () -> viewModel.onOperator("−", "-"));
        bindButton(R.id.button_multiply, () -> viewModel.onOperator("×", "*"));
        bindButton(R.id.button_divide, () -> viewModel.onOperator("÷", "/"));
        bindButton(R.id.button_percent, () -> viewModel.onPercent());
        bindButton(R.id.button_power, () -> viewModel.onOperator("^", "^"));

        bindButton(R.id.button_sin, () -> viewModel.onFunction("sin", "sin"));
        bindButton(R.id.button_cos, () -> viewModel.onFunction("cos", "cos"));
        bindButton(R.id.button_tan, () -> viewModel.onFunction("tan", "tan"));
        bindButton(R.id.button_ln, () -> viewModel.onFunction("ln", "ln"));
        bindButton(R.id.button_log, () -> viewModel.onFunction("log", "log"));
        bindButton(R.id.button_sqrt, () -> viewModel.onFunction("√", "sqrt"));
        bindButton(R.id.button_pi, () -> viewModel.onPi());

        bindButton(R.id.button_ac, () -> viewModel.onAllClear());
        bindButton(R.id.button_plus_minus, () -> viewModel.onBackspace());
        bindButton(R.id.button_equals, () -> viewModel.onEquals());
    }

    private void bindDigitButton(CalculatorViewModel viewModel, int buttonId, String digit) {
        bindButton(buttonId, () -> viewModel.onDigit(digit));
    }

    private void bindButton(int buttonId, Runnable action) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            animateButton(v);
            action.run();
        });
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start())
                .start();
    }
}