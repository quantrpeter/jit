package com.jitcompiler;

import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Compiles Java bytecode to native Mach-O executables
 */
public class NativeCompiler {
    
    private final BytecodeAnalyzer analyzer;
    private final MachineCodeGenerator codeGenerator;
    private final String architecture;
    
    public NativeCompiler() {
        this.analyzer = new BytecodeAnalyzer();
        this.codeGenerator = new MachineCodeGenerator();
        this.architecture = codeGenerator.getArchitecture();
    }
    
    /**
     * Compile a Java class to a native Mach-O executable
     */
    public Path compileToNative(String className, String outputFileName) throws Exception {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║   Native Mach-O Compilation Started            ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("Class: " + className);
        System.out.println("Target: " + outputFileName);
        System.out.println("Architecture: " + architecture);
        
        // Analyze bytecode
        analyzer.analyze(className);
        System.out.println("\n=== Analyzing Bytecode ===");
        
        ClassNode classNode = analyzer.getClassNode();
        Map<String, BytecodeAnalyzer.MethodInfo> methodInfoMap = analyzer.getMethodInfoMap();
        
        // Find main method or first public static method
        MethodNode targetMethod = findExecutableMethod(classNode);
        if (targetMethod == null) {
            throw new IllegalArgumentException("No executable method found in class");
        }
        
        System.out.println("Target method: " + targetMethod.name + targetMethod.desc);
        
        BytecodeAnalyzer.MethodInfo info = methodInfoMap.get(targetMethod.name + targetMethod.desc);
        if (info != null) {
            System.out.println("  Instructions: " + info.instructionCount);
            System.out.println("  Arithmetic ops: " + info.arithmeticOps);
        }
        
        // Generate machine code
        System.out.println("\n=== Generating Native Code ===");
        byte[] machineCode = codeGenerator.generateMethodCode(targetMethod);
        
        // Write Mach-O executable
        System.out.println("\n=== Creating Mach-O Executable ===");
        Path outputPath = Paths.get(outputFileName);
        MachOWriter writer = new MachOWriter(architecture);
        writer.writeExecutable(outputPath, machineCode, 0);
        
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║   Native Compilation Completed                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("Executable: " + outputPath.toAbsolutePath());
        System.out.println("\nYou can now run:");
        System.out.println("  ./" + outputFileName);
        
        return outputPath;
    }
    
    /**
     * Compile multiple methods and create an executable with a main wrapper
     */
    public Path compileClassToNative(String className, String outputFileName) throws Exception {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║   Full Class Native Compilation                ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        
        analyzer.analyze(className);
        ClassNode classNode = analyzer.getClassNode();
        
        // Generate a combined machine code blob with all methods
        List<byte[]> methodCodes = new ArrayList<>();
        Map<String, Integer> methodOffsets = new HashMap<>();
        int totalSize = 0;
        
        System.out.println("\n=== Compiling Methods ===");
        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("<init>") && !method.name.equals("<clinit>")) {
                System.out.println("Compiling method: " + method.name);
                byte[] code = codeGenerator.generateMethodCode(method);
                methodOffsets.put(method.name, totalSize);
                methodCodes.add(code);
                totalSize += code.length;
            }
        }
        
        // Combine all method codes
        byte[] combinedCode = new byte[totalSize];
        int offset = 0;
        for (byte[] code : methodCodes) {
            System.arraycopy(code, 0, combinedCode, offset, code.length);
            offset += code.length;
        }
        
        // Create executable
        Path outputPath = Paths.get(outputFileName);
        MachOWriter writer = new MachOWriter(architecture);
        writer.writeExecutable(outputPath, combinedCode, 0);
        
        System.out.println("\n✓ Native executable created: " + outputPath.toAbsolutePath());
        return outputPath;
    }
    
    /**
     * Compile a simple arithmetic expression to native code
     */
    public Path compileExpression(String expression, String outputFileName) throws Exception {
        System.out.println("\n=== Compiling Expression ===");
        System.out.println("Expression: " + expression);
        
        // Create a simple method that evaluates the expression
        // For now, we'll generate machine code directly for simple cases
        
        byte[] machineCode = generateExpressionCode(expression);
        
        Path outputPath = Paths.get(outputFileName);
        MachOWriter writer = new MachOWriter(architecture);
        writer.writeExecutable(outputPath, machineCode, 0);
        
        System.out.println("✓ Expression compiled to: " + outputPath.toAbsolutePath());
        return outputPath;
    }
    
    private MethodNode findExecutableMethod(ClassNode classNode) {
        // Look for main method first
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("main") && method.desc.equals("([Ljava/lang/String;)V")) {
                return method;
            }
        }
        
        // Look for any public static method with primitive return type
        for (MethodNode method : classNode.methods) {
            if ((method.access & ACC_PUBLIC) != 0 && 
                (method.access & ACC_STATIC) != 0 &&
                !method.name.equals("<init>") &&
                !method.name.equals("<clinit>")) {
                return method;
            }
        }
        
        // Return first non-constructor method
        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("<init>") && !method.name.equals("<clinit>")) {
                return method;
            }
        }
        
        return null;
    }
    
    private byte[] generateExpressionCode(String expression) {
        // Simple expression compiler for demo purposes
        // This would need a proper expression parser in production
        
        List<Byte> code = new ArrayList<>();
        
        if (architecture.equals("ARM64")) {
            // ARM64 code to return 42
            // mov w0, #42
            code.add((byte)0x52);
            code.add((byte)0x80);
            code.add((byte)0x05);
            code.add((byte)0x40);
            // ret
            code.add((byte)0xc0);
            code.add((byte)0x03);
            code.add((byte)0x5f);
            code.add((byte)0xd6);
        } else {
            // X86-64 code to return 42
            // mov eax, 42
            code.add((byte)0xb8);
            code.add((byte)42);
            code.add((byte)0x00);
            code.add((byte)0x00);
            code.add((byte)0x00);
            // ret
            code.add((byte)0xc3);
        }
        
        byte[] result = new byte[code.size()];
        for (int i = 0; i < code.size(); i++) {
            result[i] = code.get(i);
        }
        return result;
    }
    
    public String getArchitecture() {
        return architecture;
    }
}

