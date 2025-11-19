# Java Bytecode JIT Compiler

A simple Just-In-Time (JIT) compiler that compiles Java class bytecode into optimized executable code using Java and Maven.

## Overview

This project demonstrates a JIT compiler implementation in Java that:
- Reads and analyzes Java bytecode from `.class` files
- Applies optimizations (constant folding, dead code elimination)
- Dynamically compiles and loads optimized bytecode
- Executes the compiled code at runtime

## Features

- ✅ **Bytecode Analysis**: Deep inspection of Java bytecode structure
- ✅ **Hot Method Detection**: Identifies methods that would benefit from JIT compilation
- ✅ **Optimization**: Applies constant folding and dead code elimination
- ✅ **Dynamic Loading**: Loads and executes compiled bytecode at runtime
- ✅ **Performance Metrics**: Tracks method execution and optimization statistics

## Project Structure

```
jit/
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── jitcompiler/
    │               ├── BytecodeAnalyzer.java  # Analyzes Java bytecode
    │               ├── JitCompiler.java       # Main JIT compiler
    │               ├── Main.java              # Demo application
    │               └── samples/
    │                   ├── Calculator.java    # Sample class for compilation
    │                   └── StringProcessor.java
    └── test/
        └── java/
            └── com/
                └── jitcompiler/
                    ├── JitCompilerTest.java
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

## Running the Demo

There are several ways to run the demo:

### Option 1: Using Maven exec plugin
```bash
mvn exec:java -Dexec.mainClass="com.jitcompiler.Main"
```

### Option 2: Using the packaged JAR
```bash
mvn package
java -jar target/java-bytecode-jit-1.0-SNAPSHOT.jar
```

### Option 3: Direct execution with Maven
```bash
mvn compile exec:java
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

### JitCompiler
- Compiles bytecode with optimizations
- Uses ASM ClassWriter for bytecode generation
- Implements custom ClassLoader for dynamic loading
- Tracks compiled methods and execution statistics

### Sample Classes
- `Calculator`: Mathematical operations for JIT compilation
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
- [ ] Native code generation (via LLVM or similar)
- [ ] Tiered compilation
- [ ] Profile-guided optimization
- [ ] Escape analysis

## License

This project is provided for educational purposes.

## Contributing

Feel free to fork this project and experiment with additional optimizations!

## Resources

- [ASM Library Documentation](https://asm.ow2.io/)
- [JVM Specification](https://docs.oracle.com/javase/specs/jvms/se17/html/)
- [Java Bytecode Instructions](https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings)

## Author

Created as a demonstration of JIT compilation concepts using Java and Maven.

