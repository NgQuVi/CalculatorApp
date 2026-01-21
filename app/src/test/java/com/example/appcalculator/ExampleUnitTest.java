package com.example.appcalculator;

import java.math.BigDecimal;

import com.example.appcalculator.utils.ExpressionEvaluator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


public class ExampleUnitTest {
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Test
    public void evaluatesBasicAddition() {
        assertEquals(0, evaluator.evaluate("1+1").compareTo(new BigDecimal("2")));
    }

    @Test
    public void respectsOperatorPrecedence() {
        assertEquals(0, evaluator.evaluate("1+2*3").compareTo(new BigDecimal("7")));
        assertEquals(0, evaluator.evaluate("(1+2)*3").compareTo(new BigDecimal("9")));
    }

    @Test
    public void evaluatesScientificFunctions() {
        assertTrue(evaluator.evaluate("sin(0)").compareTo(BigDecimal.ZERO) == 0);
        assertEquals(0, evaluator.evaluate("pow(2,3)").compareTo(new BigDecimal("8")));
        assertEquals(0, evaluator.evaluate("sqrt(9)").compareTo(new BigDecimal("3")));
    }

    @Test
    public void divisionByZeroThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("1/0"));
    }
}