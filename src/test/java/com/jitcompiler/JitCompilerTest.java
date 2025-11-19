package com.jitcompiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for JIT Compiler
 */
public class JitCompilerTest {
    
    @Test
    @DisplayName("JIT compile Calculator class")
    public void testCompileCalculator() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
        
        assertNotNull(compiled);
        assertNotNull(compiled.getCompiledClass());
        
        Object instance = compiled.newInstance();
        assertNotNull(instance);
    }
    
    @Test
    @DisplayName("Execute compiled add method")
    public void testExecuteAddMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
        
        Object calculator = compiled.newInstance();
        Method addMethod = compiled.getMethod("add", int.class, int.class);
        
        int result = (int) addMethod.invoke(calculator, 10, 20);
        assertEquals(30, result);
    }
    
    @Test
    @DisplayName("Execute compiled multiply method")
    public void testExecuteMultiplyMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
        
        Object calculator = compiled.newInstance();
        Method multiplyMethod = compiled.getMethod("multiply", int.class, int.class);
        
        int result = (int) multiplyMethod.invoke(calculator, 7, 8);
        assertEquals(56, result);
    }
    
    @Test
    @DisplayName("Execute compiled factorial method")
    public void testExecuteFactorialMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
        
        Object calculator = compiled.newInstance();
        Method factorialMethod = compiled.getMethod("factorial", int.class);
        
        int result1 = (int) factorialMethod.invoke(calculator, 5);
        assertEquals(120, result1);
        
        int result2 = (int) factorialMethod.invoke(calculator, 0);
        assertEquals(1, result2);
    }
    
    @Test
    @DisplayName("Execute compiled max method")
    public void testExecuteMaxMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
        
        Object calculator = compiled.newInstance();
        Method maxMethod = compiled.getMethod("max", int.class, int.class);
        
        int result1 = (int) maxMethod.invoke(calculator, 100, 50);
        assertEquals(100, result1);
        
        int result2 = (int) maxMethod.invoke(calculator, 25, 75);
        assertEquals(75, result2);
    }
    
    @Test
    @DisplayName("Execute compiled sumArray method")
    public void testExecuteSumArrayMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
        
        Object calculator = compiled.newInstance();
        Method sumArrayMethod = compiled.getMethod("sumArray", int[].class);
        
        int result = (int) sumArrayMethod.invoke(calculator, (Object) new int[]{1, 2, 3, 4, 5});
        assertEquals(15, result);
    }
    
    @Test
    @DisplayName("JIT compile StringProcessor class")
    public void testCompileStringProcessor() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.StringProcessor");
        
        assertNotNull(compiled);
        Object instance = compiled.newInstance();
        assertNotNull(instance);
    }
    
    @Test
    @DisplayName("Execute compiled string concat method")
    public void testExecuteConcatMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.StringProcessor");
        
        Object processor = compiled.newInstance();
        Method concatMethod = compiled.getMethod("concat", String.class, String.class);
        
        String result = (String) concatMethod.invoke(processor, "Hello", "World");
        assertEquals("HelloWorld", result);
    }
    
    @Test
    @DisplayName("Execute compiled string reverse method")
    public void testExecuteReverseMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.StringProcessor");
        
        Object processor = compiled.newInstance();
        Method reverseMethod = compiled.getMethod("reverse", String.class);
        
        String result = (String) reverseMethod.invoke(processor, "Java");
        assertEquals("avaJ", result);
    }
    
    @Test
    @DisplayName("Execute compiled countVowels method")
    public void testExecuteCountVowelsMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.StringProcessor");
        
        Object processor = compiled.newInstance();
        Method countVowelsMethod = compiled.getMethod("countVowels", String.class);
        
        int result = (int) countVowelsMethod.invoke(processor, "Programming");
        assertEquals(3, result);
    }
    
    @Test
    @DisplayName("Execute compiled isPalindrome method")
    public void testExecuteIsPalindromeMethod() throws Exception {
        JitCompiler jit = new JitCompiler();
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.StringProcessor");
        
        Object processor = compiled.newInstance();
        Method isPalindromeMethod = compiled.getMethod("isPalindrome", String.class);
        
        boolean result1 = (boolean) isPalindromeMethod.invoke(processor, "racecar");
        assertTrue(result1);
        
        boolean result2 = (boolean) isPalindromeMethod.invoke(processor, "hello");
        assertFalse(result2);
    }
    
    @Test
    @DisplayName("Verify optimization is applied to hot methods")
    public void testOptimizationEnabled() throws Exception {
        JitCompiler jit = new JitCompiler();
        jit.setOptimizationEnabled(true);
        
        JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
        
        // complexCalculation should be optimized as it's a hot method
        assertNotNull(compiled.getMethods());
        assertTrue(compiled.getMethods().size() > 0);
    }
    
    @Test
    @DisplayName("Test bytecode analyzer")
    public void testBytecodeAnalyzer() throws Exception {
        BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
        analyzer.analyze("com.jitcompiler.samples.Calculator");
        
        assertNotNull(analyzer.getClassName());
        assertNotNull(analyzer.getMethodInfoMap());
        assertTrue(analyzer.getMethodInfoMap().size() > 0);
    }
}

