# Java Bytecode JIT Compiler

A JIT compiler that compiles Java class bytecode into both optimized bytecode and native ELF executables using Java and Maven.

## Overview

This project demonstrates two compilation modes:

### 1. JIT Mode (Bytecode Optimization)
- Reads and analyzes Java bytecode from `.class` files
- Applies optimizations (constant folding, dead code elimination)
- Dynamically compiles and loads optimized bytecode
- Executes the compiled code at runtime

### 2. Native Mode (ELF Executable Generation)
- Compiles Java bytecode to native x86-64 machine code
- Generates standalone ELF executables for Linux
- Produces real binaries that can run without a JVM
- Supports Docker execution on macOS

## Features

### JIT Compilation
- ✅ **Bytecode Analysis**: Deep inspection of Java bytecode structure
- ✅ **Hot Method Detection**: Identifies methods that would benefit from JIT compilation
- ✅ **Optimization**: Applies constant folding and dead code elimination
- ✅ **Dynamic Loading**: Loads and executes compiled bytecode at runtime
- ✅ **Performance Metrics**: Tracks method execution and optimization statistics

### Native Compilation
- ✅ **Machine Code Generation**: Converts bytecode to native x86-64 assembly
- ✅ **ELF Executable Format**: Generates Linux-compatible binaries
- ✅ **Standalone Executables**: No JVM required for execution
- ✅ **Docker Support**: Run ELF binaries on macOS via Docker
- ✅ **Multiple Architectures**: Support for x86-64 and ARM64 code generation

## Project Structure

```
jit/
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
├── Dockerfile                                 # Docker image for running ELF executables
├── run_docker.sh                              # Script to build and run in Docker
├── test_native.sh                             # Script to test native compilation
├── output/                                    # Generated ELF executables
│   ├── simple_expr                            # Native executable returning 42
│   ├── calculator                             # Native Calculator.add() method
│   └── calculator_full                        # Full Calculator class
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── jitcompiler/
    │               ├── BytecodeAnalyzer.java      # Analyzes Java bytecode
    │               ├── JitCompiler.java           # JIT bytecode compiler
    │               ├── NativeCompiler.java        # Native code compiler
    │               ├── MachineCodeGenerator.java  # x86-64/ARM64 code generation
    │               ├── ELFWriter.java             # ELF executable writer
    │               ├── Main.java                  # JIT demo application
    │               ├── NativeMain.java            # Native compilation demo
    │               └── samples/
    │                   ├── Calculator.java        # Sample class for compilation
    │                   └── StringProcessor.java
    └── test/
        └── java/
            └── com/
                └── jitcompiler/
                    ├── JitCompilerTest.java
                    ├── NativeCompilerTest.java
                    └── BytecodeAnalyzerTest.java
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

```bash
# Clone or navigate to the project directory
cd jit

# Compile the project
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn package
```

## Running the Demos

### JIT Compilation Demo

Run the JIT bytecode compiler:

```bash
# Using Maven exec plugin (default)
mvn compile exec:java

# Or with explicit main class
mvn exec:java -Dexec.mainClass="com.jitcompiler.Main"

# Or using packaged JAR
mvn package
java -jar target/java-bytecode-jit-1.0-SNAPSHOT.jar
```

### Native Compilation Demo

Generate native ELF executables:

```bash
# Run native compilation demo
mvn compile exec:java@native

# This generates three executables in output/:
# - simple_expr      (returns 42)
# - calculator       (Calculator.add method)
# - calculator_full  (full Calculator class)
```

### Running ELF Executables

#### On Linux
```bash
# Executables run directly on Linux
./output/simple_expr
echo $?  # Shows exit code: 42

./output/calculator
echo $?  # Shows exit code: 0
```

#### On macOS (using Docker)
```bash
# Build Docker image and run all executables
./run_docker.sh

# Or manually:
docker build -t jit-elf-runner .
docker run --rm jit-elf-runner /app/simple_expr
echo $?  # Shows exit code

# Interactive shell
docker run --rm -it jit-elf-runner bash
# Then: /app/calculator
```

## How It Works

### 1. Bytecode Analysis

The `BytecodeAnalyzer` reads Java `.class` files and extracts information about:
- Method instructions and their types
- Arithmetic operations
- Method calls and field accesses
- Branch instructions
- Hot method detection

```java
BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
analyzer.analyze("com.jitcompiler.samples.Calculator");
analyzer.printBytecode();
```

### 2. JIT Compilation

The `JitCompiler` performs:
1. Bytecode analysis
2. Optimization of hot methods
3. Dynamic class generation
4. Runtime loading and execution

```java
JitCompiler jit = new JitCompiler();
JitCompiler.CompiledClass compiled = jit.compile("com.jitcompiler.samples.Calculator");
Object instance = compiled.newInstance();
```

### 3. Optimizations

#### Constant Folding
Precomputes constant expressions at compile time:
```java
// Before optimization:
int a = 5 + 3;  // Multiple instructions

// After optimization:
int a = 8;      // Single instruction
```

#### Dead Code Elimination
Removes unreachable code after return statements:
```java
// Before optimization:
return x;
unreachableCode();  // Removed by JIT

// After optimization:
return x;
```

## Sample Output

```
╔═══════════════════════════════════════════════════╗
║   Java Bytecode JIT Compiler Demo                ║
╚═══════════════════════════════════════════════════╝

=== JIT Compilation Started ===
Class: com.jitcompiler.samples.Calculator

=== Bytecode Analysis ===
Class: com/jitcompiler/samples/Calculator
Methods:
  add(II)I
    Instructions: 4
    Arithmetic ops: 1
    Method calls: 0
    Branches: 0
    Field accesses: 0
  ...

JIT: Optimizing hot method: complexCalculation
  Optimization: Constant folding 5 + 3 = 8
  Optimization: Constant folding 10 + 2 = 12

=== JIT Compilation Completed ===

▶ Executing compiled methods:
  add(15, 25) = 40
  multiply(7, 8) = 56
  complexCalculation(5) = 52
    (Note: JIT optimized constant folding in this method)
  factorial(6) = 720
  max(42, 37) = 42
  sumArray([1,2,3,4,5]) = 15
```

## Architecture

### BytecodeAnalyzer
- Uses ASM library to read and parse bytecode
- Builds a method information map
- Identifies optimization opportunities
- Detects hot methods for JIT compilation

### JitCompiler
- Compiles bytecode with optimizations
- Uses ASM ClassWriter for bytecode generation
- Implements custom ClassLoader for dynamic loading
- Tracks compiled methods and execution statistics

### NativeCompiler
- Compiles Java bytecode to native machine code
- Generates x86-64 or ARM64 assembly instructions
- Creates standalone ELF executables
- No JVM required for execution

### MachineCodeGenerator
- Translates Java bytecode operations to native instructions
- Supports x86-64 and ARM64 architectures
- Implements register allocation and stack management
- Generates optimized assembly for arithmetic operations

### ELFWriter
- Creates Linux ELF64 executable files
- Writes proper ELF headers and program headers
- Wraps machine code with _start entry point
- Implements exit syscall wrapper (syscall 60)
- Sets executable permissions

### Sample Classes
- `Calculator`: Mathematical operations for compilation
- `StringProcessor`: String manipulation demonstrations

## Testing

Run the comprehensive test suite:

```bash
mvn test
```

Tests include:
- Bytecode analysis correctness
- JIT compilation and execution
- Optimization verification
- Method invocation accuracy
- Hot method detection

## Dependencies

- **ASM 9.6**: Bytecode manipulation and analysis
- **JUnit 5.10.1**: Unit testing framework

## Use Cases

This JIT compiler is useful for:
- Educational purposes (learning JIT compilation)
- Dynamic code optimization
- Runtime bytecode transformation
- Performance analysis and profiling
- Custom DSL execution

## Limitations

This is a simplified JIT compiler for demonstration purposes. Production JIT compilers (like HotSpot) include:
- More sophisticated optimization techniques
- Adaptive optimization based on runtime profiling
- Native code generation
- Advanced garbage collection integration
- Deoptimization support

## Future Enhancements

Potential improvements:
- [ ] Method inlining
- [ ] Loop unrolling
- [x] Native code generation (ELF x86-64 executables)
- [ ] Tiered compilation
- [ ] Profile-guided optimization
- [ ] Escape analysis
- [ ] Native ARM64 executable testing
- [ ] Parameter passing for native executables
- [ ] More complex bytecode instruction support
- [ ] Dynamic linking and library support

## License

This project is provided for educational purposes.

## Contributing

Feel free to fork this project and experiment with additional optimizations!

## Docker Support

Since ELF executables are Linux binaries, macOS users can use Docker:

1. **Dockerfile** provides an Ubuntu 22.04 container
2. **run_docker.sh** automates building and running executables
3. All generated binaries are copied to `/app/` in the container
4. Exit codes are properly returned (e.g., 42 for `simple_expr`)

```bash
# Quick test
./run_docker.sh

# Custom execution
docker run --rm jit-elf-runner /app/calculator
```

## Resources

- [ASM Library Documentation](https://asm.ow2.io/)
- [JVM Specification](https://docs.oracle.com/javase/specs/jvms/se17/html/)
- [Java Bytecode Instructions](https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings)
- [ELF Format Specification](https://en.wikipedia.org/wiki/Executable_and_Linkable_Format)
- [x86-64 Instruction Reference](https://www.felixcloutier.com/x86/)
- [Linux System Call Table](https://chromium.googlesource.com/chromiumos/docs/+/master/constants/syscalls.md)

## Author

Created as a demonstration of JIT compilation concepts using Java and Maven.

