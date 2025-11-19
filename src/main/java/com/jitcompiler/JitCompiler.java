package com.jitcompiler;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * JIT Compiler that compiles and optimizes Java bytecode
 */
public class JitCompiler {
    
    private final BytecodeAnalyzer analyzer;
    private final Map<String, CompiledMethod> compiledMethods;
    private final ClassLoader classLoader;
    private boolean optimizationEnabled;
    
    public JitCompiler() {
        this.analyzer = new BytecodeAnalyzer();
        this.compiledMethods = new HashMap<>();
        this.classLoader = new DynamicClassLoader();
        this.optimizationEnabled = true;
    }
    
    /**
     * Compile a class file
     */
    public CompiledClass compile(String className) throws Exception {
        System.out.println("=== JIT Compilation Started ===");
        System.out.println("Class: " + className);
        
        // Analyze the bytecode
        analyzer.analyze(className);
        analyzer.printBytecode();
        
        // Compile the class
        byte[] optimizedBytecode = compileClass(analyzer.getClassNode());
        
        // Load the compiled class
        Class<?> compiledClass = loadClass(className, optimizedBytecode);
        
        System.out.println("=== JIT Compilation Completed ===\n");
        
        return new CompiledClass(compiledClass, compiledMethods);
    }
    
    /**
     * Compile a class from input stream
     */
    public CompiledClass compile(InputStream classFileStream) throws Exception {
        analyzer.analyze(classFileStream);
        analyzer.printBytecode();
        
        byte[] optimizedBytecode = compileClass(analyzer.getClassNode());
        String className = analyzer.getClassName().replace('/', '.');
        Class<?> compiledClass = loadClass(className, optimizedBytecode);
        
        return new CompiledClass(compiledClass, compiledMethods);
    }
    
    private byte[] compileClass(ClassNode classNode) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        // Visit class header
        cw.visit(classNode.version, classNode.access, classNode.name, 
                 classNode.signature, classNode.superName, 
                 classNode.interfaces.toArray(new String[0]));
        
        // Copy fields
        if (classNode.fields != null) {
            for (FieldNode field : classNode.fields) {
                field.accept(cw);
            }
        }
        
        // Compile methods
        if (classNode.methods != null) {
            for (MethodNode method : classNode.methods) {
                compileMethod(cw, method, classNode.name);
            }
        }
        
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    private void compileMethod(ClassWriter cw, MethodNode method, String className) {
        String methodKey = method.name + method.desc;
        BytecodeAnalyzer.MethodInfo info = analyzer.getMethodInfoMap().get(methodKey);
        
        boolean shouldOptimize = optimizationEnabled && info != null && info.isHotMethod();
        
        if (shouldOptimize) {
            System.out.println("JIT: Optimizing hot method: " + method.name);
        }
        
        MethodVisitor mv = cw.visitMethod(method.access, method.name, method.desc, 
                                          method.signature, 
                                          method.exceptions.toArray(new String[0]));
        
        if (method.instructions.size() > 0) {
            // Apply optimizations if needed
            if (shouldOptimize) {
                optimizeMethod(method);
            }
            
            // Write the method bytecode
            method.accept(mv);
            
            // Track compiled method
            compiledMethods.put(methodKey, new CompiledMethod(method.name, method.desc, shouldOptimize));
        } else {
            mv.visitEnd();
        }
    }
    
    /**
     * Apply optimizations to the method
     */
    private void optimizeMethod(MethodNode method) {
        // Optimization 1: Constant folding for simple arithmetic
        constantFolding(method);
        
        // Optimization 2: Dead code elimination
        removeDeadCode(method);
    }
    
    /**
     * Simple constant folding optimization
     */
    private void constantFolding(MethodNode method) {
        InsnList instructions = method.instructions;
        AbstractInsnNode[] insnArray = instructions.toArray();
        
        for (int i = 0; i < insnArray.length - 2; i++) {
            AbstractInsnNode insn1 = insnArray[i];
            AbstractInsnNode insn2 = insnArray[i + 1];
            AbstractInsnNode insn3 = insnArray[i + 2];
            
            // Look for pattern: ICONST/BIPUSH -> ICONST/BIPUSH -> IADD
            if (isIntConstant(insn1) && isIntConstant(insn2) && 
                insn3.getOpcode() == IADD) {
                
                int val1 = getIntConstant(insn1);
                int val2 = getIntConstant(insn2);
                int result = val1 + val2;
                
                // Replace with single constant
                instructions.remove(insn1);
                instructions.remove(insn2);
                instructions.set(insn3, new LdcInsnNode(result));
                
                System.out.println("  Optimization: Constant folding " + val1 + " + " + val2 + " = " + result);
            }
        }
    }
    
    private boolean isIntConstant(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        return (opcode >= ICONST_M1 && opcode <= ICONST_5) || 
               (insn instanceof IntInsnNode && opcode == BIPUSH) ||
               (insn instanceof LdcInsnNode && ((LdcInsnNode)insn).cst instanceof Integer);
    }
    
    private int getIntConstant(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        if (opcode >= ICONST_M1 && opcode <= ICONST_5) {
            return opcode - ICONST_0;
        } else if (insn instanceof IntInsnNode) {
            return ((IntInsnNode) insn).operand;
        } else if (insn instanceof LdcInsnNode) {
            return (Integer) ((LdcInsnNode) insn).cst;
        }
        return 0;
    }
    
    /**
     * Remove unreachable code after return statements
     */
    private void removeDeadCode(MethodNode method) {
        InsnList instructions = method.instructions;
        AbstractInsnNode[] insnArray = instructions.toArray();
        
        for (int i = 0; i < insnArray.length - 1; i++) {
            AbstractInsnNode insn = insnArray[i];
            
            // If we find a RETURN instruction
            if (insn.getOpcode() >= IRETURN && insn.getOpcode() <= RETURN) {
                AbstractInsnNode next = insn.getNext();
                
                // Remove instructions until next label or end
                while (next != null && !(next instanceof LabelNode)) {
                    AbstractInsnNode toRemove = next;
                    next = next.getNext();
                    
                    if (!(toRemove instanceof LineNumberNode) && 
                        !(toRemove instanceof FrameNode)) {
                        instructions.remove(toRemove);
                        System.out.println("  Optimization: Removed dead code after return");
                        break;
                    }
                }
            }
        }
    }
    
    private Class<?> loadClass(String className, byte[] bytecode) {
        return ((DynamicClassLoader) classLoader).defineClass(className, bytecode);
    }
    
    public void setOptimizationEnabled(boolean enabled) {
        this.optimizationEnabled = enabled;
    }
    
    public BytecodeAnalyzer getAnalyzer() {
        return analyzer;
    }
    
    /**
     * Custom class loader for loading compiled classes
     */
    private static class DynamicClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
    
    /**
     * Represents a compiled class
     */
    public static class CompiledClass {
        private final Class<?> clazz;
        private final Map<String, CompiledMethod> methods;
        
        public CompiledClass(Class<?> clazz, Map<String, CompiledMethod> methods) {
            this.clazz = clazz;
            this.methods = new HashMap<>(methods);
        }
        
        public Class<?> getCompiledClass() {
            return clazz;
        }
        
        public Object newInstance() throws Exception {
            return clazz.getDeclaredConstructor().newInstance();
        }
        
        public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
            return clazz.getMethod(name, parameterTypes);
        }
        
        public Map<String, CompiledMethod> getMethods() {
            return methods;
        }
    }
    
    /**
     * Represents a compiled method
     */
    public static class CompiledMethod {
        private final String name;
        private final String descriptor;
        private final boolean optimized;
        private int executionCount;
        
        public CompiledMethod(String name, String descriptor, boolean optimized) {
            this.name = name;
            this.descriptor = descriptor;
            this.optimized = optimized;
            this.executionCount = 0;
        }
        
        public void incrementExecutionCount() {
            executionCount++;
        }
        
        public boolean isOptimized() {
            return optimized;
        }
        
        public int getExecutionCount() {
            return executionCount;
        }
    }
}

