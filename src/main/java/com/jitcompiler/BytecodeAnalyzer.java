package com.jitcompiler;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Analyzes Java bytecode to gather information for JIT compilation
 */
public class BytecodeAnalyzer {
    
    private ClassNode classNode;
    private String className;
    private Map<String, MethodInfo> methodInfoMap;
    
    public BytecodeAnalyzer() {
        this.methodInfoMap = new HashMap<>();
    }
    
    /**
     * Analyze bytecode from a class file
     */
    public void analyze(InputStream classFileStream) throws IOException {
        ClassReader classReader = new ClassReader(classFileStream);
        classNode = new ClassNode();
        classReader.accept(classNode, 0);
        
        className = classNode.name;
        
        // Analyze each method
        for (MethodNode method : classNode.methods) {
            analyzeMethod(method);
        }
    }
    
    /**
     * Analyze bytecode from a class name
     */
    public void analyze(String className) throws IOException {
        String resourceName = className.replace('.', '/') + ".class";
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            throw new IOException("Class not found: " + className);
        }
        analyze(stream);
    }
    
    private void analyzeMethod(MethodNode method) {
        MethodInfo info = new MethodInfo();
        info.name = method.name;
        info.descriptor = method.desc;
        info.access = method.access;
        
        // Count instructions
        if (method.instructions != null) {
            info.instructionCount = method.instructions.size();
            
            // Analyze instruction types
            for (AbstractInsnNode insn : method.instructions) {
                analyzeInstruction(insn, info);
            }
        }
        
        methodInfoMap.put(method.name + method.desc, info);
    }
    
    private void analyzeInstruction(AbstractInsnNode insn, MethodInfo info) {
        int opcode = insn.getOpcode();
        
        if (opcode >= Opcodes.IADD && opcode <= Opcodes.DREM) {
            info.arithmeticOps++;
        } else if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            info.returnCount++;
        } else if (insn instanceof MethodInsnNode) {
            info.methodCallCount++;
        } else if (insn instanceof FieldInsnNode) {
            info.fieldAccessCount++;
        } else if (insn instanceof JumpInsnNode) {
            info.branchCount++;
        }
    }
    
    /**
     * Print detailed bytecode information
     */
    public void printBytecode() {
        if (classNode == null) {
            System.out.println("No bytecode analyzed yet.");
            return;
        }
        
        System.out.println("\n=== Bytecode Analysis ===");
        System.out.println("Class: " + className);
        System.out.println("Methods:");
        
        for (Map.Entry<String, MethodInfo> entry : methodInfoMap.entrySet()) {
            MethodInfo info = entry.getValue();
            System.out.printf("  %s%s%n", info.name, info.descriptor);
            System.out.printf("    Instructions: %d%n", info.instructionCount);
            System.out.printf("    Arithmetic ops: %d%n", info.arithmeticOps);
            System.out.printf("    Method calls: %d%n", info.methodCallCount);
            System.out.printf("    Branches: %d%n", info.branchCount);
            System.out.printf("    Field accesses: %d%n", info.fieldAccessCount);
        }
        System.out.println("========================\n");
    }
    
    /**
     * Print human-readable bytecode
     */
    public void printReadableBytecode() throws IOException {
        if (classNode == null) {
            System.out.println("No bytecode analyzed yet.");
            return;
        }
        
        System.out.println("\n=== Disassembled Bytecode ===");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        TraceClassVisitor tcv = new TraceClassVisitor(pw);
        classNode.accept(tcv);
        System.out.println(sw.toString());
        System.out.println("============================\n");
    }
    
    public ClassNode getClassNode() {
        return classNode;
    }
    
    public String getClassName() {
        return className;
    }
    
    public Map<String, MethodInfo> getMethodInfoMap() {
        return methodInfoMap;
    }
    
    /**
     * Information about a method
     */
    public static class MethodInfo {
        public String name;
        public String descriptor;
        public int access;
        public int instructionCount;
        public int arithmeticOps;
        public int methodCallCount;
        public int fieldAccessCount;
        public int branchCount;
        public int returnCount;
        
        /**
         * Determine if method is hot enough for JIT compilation
         */
        public boolean isHotMethod() {
            // Simple heuristic: methods with more than 10 instructions
            // or complex operations are candidates for JIT compilation
            return instructionCount > 10 || arithmeticOps > 3 || branchCount > 2;
        }
    }
}

