package com.example.appcalculator;

import android.os.Bundle;
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

        viewModel.getExpression().observe(this, expressionView::setText);
        viewModel.getResult().observe(this, resultView::setText);

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

        findViewById(R.id.button_decimal).setOnClickListener(v -> viewModel.onDecimal());
        findViewById(R.id.button_plus).setOnClickListener(v -> viewModel.onOperator("+", "+"));
        findViewById(R.id.button_minus).setOnClickListener(v -> viewModel.onOperator("−", "-"));
        findViewById(R.id.button_multiply).setOnClickListener(v -> viewModel.onOperator("×", "*"));
        findViewById(R.id.button_divide).setOnClickListener(v -> viewModel.onOperator("÷", "/"));
        findViewById(R.id.button_percent).setOnClickListener(v -> viewModel.onPercent());
        findViewById(R.id.button_power).setOnClickListener(v -> viewModel.onOperator("^", "^"));

        findViewById(R.id.button_sin).setOnClickListener(v -> viewModel.onFunction("sin", "sin"));
        findViewById(R.id.button_cos).setOnClickListener(v -> viewModel.onFunction("cos", "cos"));
        findViewById(R.id.button_tan).setOnClickListener(v -> viewModel.onFunction("tan", "tan"));
        findViewById(R.id.button_ln).setOnClickListener(v -> viewModel.onFunction("ln", "ln"));
        findViewById(R.id.button_log).setOnClickListener(v -> viewModel.onFunction("log", "log"));
        findViewById(R.id.button_sqrt).setOnClickListener(v -> viewModel.onFunction("√", "sqrt"));
        findViewById(R.id.button_pi).setOnClickListener(v -> viewModel.onPi());

        findViewById(R.id.button_ac).setOnClickListener(v -> viewModel.onAllClear());
        findViewById(R.id.button_plus_minus).setOnClickListener(v -> viewModel.onBackspace());
        findViewById(R.id.button_equals).setOnClickListener(v -> viewModel.onEquals());
    }

    private void bindDigitButton(CalculatorViewModel viewModel, int buttonId, String digit) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> viewModel.onDigit(digit));
    }
}