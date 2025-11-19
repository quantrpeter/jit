package com.jitcompiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for BytecodeAnalyzer
 */
public class BytecodeAnalyzerTest {
    
    @Test
    @DisplayName("Analyze Calculator class bytecode")
    public void testAnalyzeCalculator() throws Exception {
        BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
        analyzer.analyze("com.jitcompiler.samples.Calculator");
        
        assertNotNull(analyzer.getClassName());
        assertTrue(analyzer.getClassName().contains("Calculator"));
        
        Map<String, BytecodeAnalyzer.MethodInfo> methods = analyzer.getMethodInfoMap();
        assertNotNull(methods);
        assertTrue(methods.size() > 0);
    }
    
    @Test
    @DisplayName("Analyze method instruction counts")
    public void testMethodInstructionCounts() throws Exception {
        BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
        analyzer.analyze("com.jitcompiler.samples.Calculator");
        
        Map<String, BytecodeAnalyzer.MethodInfo> methods = analyzer.getMethodInfoMap();
        
        // Check that methods have instructions
        for (BytecodeAnalyzer.MethodInfo method : methods.values()) {
            if (!method.name.equals("<init>")) {
                assertTrue(method.instructionCount > 0, 
                    "Method " + method.name + " should have instructions");
            }
        }
    }
    
    @Test
    @DisplayName("Identify hot methods")
    public void testHotMethodDetection() throws Exception {
        BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
        analyzer.analyze("com.jitcompiler.samples.Calculator");
        
        Map<String, BytecodeAnalyzer.MethodInfo> methods = analyzer.getMethodInfoMap();
        
        // factorial and fibonacci should be detected as hot methods
        boolean foundHotMethod = false;
        for (BytecodeAnalyzer.MethodInfo method : methods.values()) {
            if (method.isHotMethod()) {
                foundHotMethod = true;
                break;
            }
        }
        
        assertTrue(foundHotMethod, "Should detect at least one hot method");
    }
    
    @Test
    @DisplayName("Analyze StringProcessor class")
    public void testAnalyzeStringProcessor() throws Exception {
        BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
        analyzer.analyze("com.jitcompiler.samples.StringProcessor");
        
        assertNotNull(analyzer.getClassName());
        assertTrue(analyzer.getClassName().contains("StringProcessor"));
        
        Map<String, BytecodeAnalyzer.MethodInfo> methods = analyzer.getMethodInfoMap();
        assertNotNull(methods);
        assertTrue(methods.size() > 0);
    }
    
    @Test
    @DisplayName("Method info contains branch information")
    public void testBranchAnalysis() throws Exception {
        BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
        analyzer.analyze("com.jitcompiler.samples.Calculator");
        
        Map<String, BytecodeAnalyzer.MethodInfo> methods = analyzer.getMethodInfoMap();
        
        // Find max method which has branches
        BytecodeAnalyzer.MethodInfo maxMethod = methods.values().stream()
            .filter(m -> m.name.equals("max"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(maxMethod);
        assertTrue(maxMethod.branchCount > 0, "max method should have branches");
    }
}

