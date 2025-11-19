package com.jitcompiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Native Compiler
 */
public class NativeCompilerTest {
    
    private static Path outputDir;
    
    @BeforeAll
    static void setup() throws Exception {
        outputDir = Paths.get("output");
        Files.createDirectories(outputDir);
    }
    
    @Test
    @DisplayName("Native compiler initialization")
    public void testNativeCompilerInit() {
        NativeCompiler compiler = new NativeCompiler();
        assertNotNull(compiler);
        assertNotNull(compiler.getArchitecture());
        assertTrue(compiler.getArchitecture().equals("ARM64") || 
                  compiler.getArchitecture().equals("X86_64"));
    }
    
    @Test
    @DisplayName("Compile simple expression to Mach-O")
    public void testCompileExpression() throws Exception {
        NativeCompiler compiler = new NativeCompiler();
        Path executable = outputDir.resolve("test_expr");
        
        Path result = compiler.compileExpression("42", executable.toString());
        
        assertNotNull(result);
        assertTrue(result.toFile().exists());
        assertTrue(result.toFile().canExecute());
        assertTrue(result.toFile().length() > 0);
    }
    
    @Test
    @DisplayName("Compile Calculator method to native")
    public void testCompileMethod() throws Exception {
        NativeCompiler compiler = new NativeCompiler();
        Path executable = outputDir.resolve("test_method");
        
        Path result = compiler.compileToNative(
            "com.jitcompiler.samples.Calculator",
            executable.toString()
        );
        
        assertNotNull(result);
        assertTrue(result.toFile().exists());
        assertTrue(result.toFile().canExecute());
    }
    
    @Test
    @DisplayName("Compile full class to native")
    public void testCompileClass() throws Exception {
        NativeCompiler compiler = new NativeCompiler();
        Path executable = outputDir.resolve("test_class");
        
        Path result = compiler.compileClassToNative(
            "com.jitcompiler.samples.Calculator",
            executable.toString()
        );
        
        assertNotNull(result);
        assertTrue(result.toFile().exists());
        assertTrue(result.toFile().canExecute());
        assertTrue(result.toFile().length() > 100); // Should have some code
    }
    
    @Test
    @DisplayName("Machine code generator creates code")
    public void testMachineCodeGenerator() {
        MachineCodeGenerator generator = new MachineCodeGenerator();
        assertNotNull(generator);
        assertNotNull(generator.getArchitecture());
    }
    
    @Test
    @DisplayName("Mach-O writer creates valid executable")
    public void testMachOWriter() throws Exception {
        MachOWriter writer = new MachOWriter("X86_64");
        Path executable = outputDir.resolve("test_macho");
        
        // Simple machine code that returns 0
        byte[] machineCode = new byte[]{
            (byte)0xb8, 0x00, 0x00, 0x00, 0x00,  // mov eax, 0
            (byte)0xc3                            // ret
        };
        
        writer.writeExecutable(executable, machineCode, 0);
        
        assertTrue(executable.toFile().exists());
        assertTrue(executable.toFile().canExecute());
        assertTrue(executable.toFile().length() > 100);
    }
}

