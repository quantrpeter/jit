package com.jitcompiler.samples;

/**
 * Sample class for JIT compilation demonstration
 * Contains simple arithmetic operations
 */
public class Calculator {
    
    /**
     * Simple addition
     */
    public int add(int a, int b) {
        return a + b;
    }
    
    /**
     * Simple subtraction
     */
    public int subtract(int a, int b) {
        return a - b;
    }
    
    /**
     * Multiplication
     */
    public int multiply(int a, int b) {
        return a * b;
    }
    
    /**
     * Complex calculation that should be optimized by JIT
     * Contains constant folding opportunities
     */
    public int complexCalculation(int x) {
        // These constants will be folded by the JIT compiler
        int a = 5 + 3;  // Should be optimized to 8
        int b = 10 + 2; // Should be optimized to 12
        
        return x * a + b;
    }
    
    /**
     * Method with branches
     */
    public int max(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }
    
    /**
     * Factorial calculation (recursive)
     */
    public int factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }
    
    /**
     * Fibonacci calculation
     */
    public int fibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
    
    /**
     * Sum of array elements
     */
    public int sumArray(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }
}

