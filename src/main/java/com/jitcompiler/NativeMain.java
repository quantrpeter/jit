package com.jitcompiler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

/**
 * Demo for native Mach-O compilation
 */
public class NativeMain {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   Native Mach-O Compiler Demo                    ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");
        
        try {
            // Create output directory
            Path outputDir = Paths.get("output");
            Files.createDirectories(outputDir);
            
            NativeCompiler nativeCompiler = new NativeCompiler();
            
            System.out.println("System Architecture: " + nativeCompiler.getArchitecture());
            System.out.println("OS: " + System.getProperty("os.name"));
            System.out.println("OS Arch: " + System.getProperty("os.arch"));
            System.out.println("Output Directory: " + outputDir.toAbsolutePath());
            
            // Demo 1: Compile a simple expression
            demonstrateExpressionCompilation(nativeCompiler, outputDir);
            
            System.out.println("\n" + "=".repeat(60) + "\n");
            
            // Demo 2: Compile a Calculator method
            demonstrateMethodCompilation(nativeCompiler, outputDir);
            
            System.out.println("\n" + "=".repeat(60) + "\n");
            
            // Demo 3: Compile full class
            demonstrateClassCompilation(nativeCompiler, outputDir);
            
            System.out.println("\n╔═══════════════════════════════════════════════════╗");
            System.out.println("║   Demo Complete                                   ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");
            System.out.println("\nGenerated executables in output/:");
            System.out.println("  • simple_expr      - Returns 42");
            System.out.println("  • calculator       - Calculator add method");
            System.out.println("  • calculator_full  - Full Calculator class");
            System.out.println("\nNote: These are native Mach-O executables!");
            System.out.println("Run them with: ./output/calculator");
            
        } catch (Exception e) {
            System.err.println("Error during native compilation:");
            e.printStackTrace();
        }
    }
    
    private static void demonstrateExpressionCompilation(NativeCompiler compiler, Path outputDir) {
        System.out.println("📐 DEMO 1: Simple Expression Compilation");
        System.out.println("─".repeat(60));
        
        try {
            Path executable = compiler.compileExpression("42", outputDir.resolve("simple_expr").toString());
            System.out.println("\n✓ Created native executable: " + executable.getFileName());
            System.out.println("  This is a real Mach-O binary that returns 42");
            System.out.println("  File size: " + executable.toFile().length() + " bytes");
            
            // Show file info
            showFileInfo(executable);
            
        } catch (Exception e) {
            System.err.println("Failed to compile expression: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateMethodCompilation(NativeCompiler compiler, Path outputDir) {
        System.out.println("🔢 DEMO 2: Calculator Method Compilation");
        System.out.println("─".repeat(60));
        
        try {
            Path executable = compiler.compileToNative(
                "com.jitcompiler.samples.Calculator",
                outputDir.resolve("calculator").toString()
            );
            
            System.out.println("\n✓ Created native executable: " + executable.getFileName());
            System.out.println("  Compiled from: Calculator.add() method");
            System.out.println("  File size: " + executable.toFile().length() + " bytes");
            
            showFileInfo(executable);
            
        } catch (Exception e) {
            System.err.println("Failed to compile method: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateClassCompilation(NativeCompiler compiler, Path outputDir) {
        System.out.println("📦 DEMO 3: Full Class Compilation");
        System.out.println("─".repeat(60));
        
        try {
            Path executable = compiler.compileClassToNative(
                "com.jitcompiler.samples.Calculator",
                outputDir.resolve("calculator_full").toString()
            );
            
            System.out.println("\n✓ Created native executable: " + executable.getFileName());
            System.out.println("  Compiled from: Full Calculator class");
            System.out.println("  File size: " + executable.toFile().length() + " bytes");
            System.out.println("  Contains multiple methods compiled to native code");
            
            showFileInfo(executable);
            
        } catch (Exception e) {
            System.err.println("Failed to compile class: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showFileInfo(Path executable) throws Exception {
        System.out.println("\n  File Information:");
        System.out.println("    Path: " + executable.toAbsolutePath());
        System.out.println("    Executable: " + executable.toFile().canExecute());
        
        // Try to verify it's a Mach-O file
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                "file", executable.toString()
            });
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line = reader.readLine();
            if (line != null) {
                System.out.println("    Type: " + line.substring(line.indexOf(":") + 2));
            }
        } catch (Exception e) {
            // file command not available
            System.out.println("    Type: Mach-O executable (verification skipped)");
        }
    }
}

