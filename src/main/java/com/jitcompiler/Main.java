package com.jitcompiler;

import com.jitcompiler.samples.Calculator;
import com.jitcompiler.samples.StringProcessor;

import java.lang.reflect.Method;

/**
 * Main demo class for the JIT Compiler
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   Java Bytecode JIT Compiler Demo                ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");
        
        try {
            // Demo 1: Calculator class
            demonstrateCalculator();
            
            System.out.println("\n" + "=".repeat(60) + "\n");
            
            // Demo 2: StringProcessor class
            demonstrateStringProcessor();
            
            System.out.println("\n" + "=".repeat(60) + "\n");
            
            // Demo 3: Performance comparison
            demonstratePerformance();
            
        } catch (Exception e) {
            System.err.println("Error during JIT compilation demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateCalculator() throws Exception {
        System.out.println("📊 DEMO 1: Calculator Class JIT Compilation");
        System.out.println("─".repeat(60));
        
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiledClass = jit.compile("com.jitcompiler.samples.Calculator");
        
        // Create instance and call methods
        Object calculator = compiledClass.newInstance();
        
        System.out.println("\n▶ Executing compiled methods:\n");
        
        // Test add method
        Method addMethod = compiledClass.getMethod("add", int.class, int.class);
        int result1 = (int) addMethod.invoke(calculator, 15, 25);
        System.out.println("  add(15, 25) = " + result1);
        
        // Test multiply method
        Method multiplyMethod = compiledClass.getMethod("multiply", int.class, int.class);
        int result2 = (int) multiplyMethod.invoke(calculator, 7, 8);
        System.out.println("  multiply(7, 8) = " + result2);
        
        // Test complex calculation
        Method complexMethod = compiledClass.getMethod("complexCalculation", int.class);
        int result3 = (int) complexMethod.invoke(calculator, 5);
        System.out.println("  complexCalculation(5) = " + result3);
        System.out.println("    (Note: JIT optimized constant folding in this method)");
        
        // Test factorial
        Method factorialMethod = compiledClass.getMethod("factorial", int.class);
        int result4 = (int) factorialMethod.invoke(calculator, 6);
        System.out.println("  factorial(6) = " + result4);
        
        // Test max
        Method maxMethod = compiledClass.getMethod("max", int.class, int.class);
        int result5 = (int) maxMethod.invoke(calculator, 42, 37);
        System.out.println("  max(42, 37) = " + result5);
        
        // Test sumArray
        Method sumArrayMethod = compiledClass.getMethod("sumArray", int[].class);
        int result6 = (int) sumArrayMethod.invoke(calculator, (Object) new int[]{1, 2, 3, 4, 5});
        System.out.println("  sumArray([1,2,3,4,5]) = " + result6);
    }
    
    private static void demonstrateStringProcessor() throws Exception {
        System.out.println("📝 DEMO 2: StringProcessor Class JIT Compilation");
        System.out.println("─".repeat(60));
        
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiledClass = jit.compile("com.jitcompiler.samples.StringProcessor");
        
        Object processor = compiledClass.newInstance();
        
        System.out.println("\n▶ Executing compiled methods:\n");
        
        // Test concat
        Method concatMethod = compiledClass.getMethod("concat", String.class, String.class);
        String result1 = (String) concatMethod.invoke(processor, "Hello, ", "World!");
        System.out.println("  concat(\"Hello, \", \"World!\") = \"" + result1 + "\"");
        
        // Test reverse
        Method reverseMethod = compiledClass.getMethod("reverse", String.class);
        String result2 = (String) reverseMethod.invoke(processor, "JIT Compiler");
        System.out.println("  reverse(\"JIT Compiler\") = \"" + result2 + "\"");
        
        // Test countVowels
        Method countVowelsMethod = compiledClass.getMethod("countVowels", String.class);
        int result3 = (int) countVowelsMethod.invoke(processor, "Java Programming");
        System.out.println("  countVowels(\"Java Programming\") = " + result3);
        
        // Test isPalindrome
        Method isPalindromeMethod = compiledClass.getMethod("isPalindrome", String.class);
        boolean result4 = (boolean) isPalindromeMethod.invoke(processor, "racecar");
        System.out.println("  isPalindrome(\"racecar\") = " + result4);
        
        boolean result5 = (boolean) isPalindromeMethod.invoke(processor, "hello");
        System.out.println("  isPalindrome(\"hello\") = " + result5);
    }
    
    private static void demonstratePerformance() throws Exception {
        System.out.println("⚡ DEMO 3: Performance Comparison");
        System.out.println("─".repeat(60));
        
        // Regular execution
        System.out.println("\n▶ Regular (non-JIT) execution:");
        long start1 = System.nanoTime();
        Calculator regularCalc = new Calculator();
        int sum1 = 0;
        for (int i = 0; i < 10000; i++) {
            sum1 += regularCalc.complexCalculation(i);
        }
        long end1 = System.nanoTime();
        double time1 = (end1 - start1) / 1_000_000.0;
        System.out.println("  Time: " + String.format("%.3f", time1) + " ms");
        System.out.println("  Result checksum: " + sum1);
        
        // JIT compiled execution
        System.out.println("\n▶ JIT compiled execution:");
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiledClass = jit.compile("com.jitcompiler.samples.Calculator");
        Object jitCalc = compiledClass.newInstance();
        Method method = compiledClass.getMethod("complexCalculation", int.class);
        
        long start2 = System.nanoTime();
        int sum2 = 0;
        for (int i = 0; i < 10000; i++) {
            sum2 += (int) method.invoke(jitCalc, i);
        }
        long end2 = System.nanoTime();
        double time2 = (end2 - start2) / 1_000_000.0;
        System.out.println("  Time: " + String.format("%.3f", time2) + " ms");
        System.out.println("  Result checksum: " + sum2);
        
        System.out.println("\n📈 Analysis:");
        System.out.println("  The JIT compiler optimized the bytecode with:");
        System.out.println("  • Constant folding (5+3 and 10+2 precomputed)");
        System.out.println("  • Dead code elimination");
        System.out.println("  • Method inlining opportunities identified");
        
        if (sum1 == sum2) {
            System.out.println("\n  ✓ Both methods produced identical results!");
        }
    }
}

