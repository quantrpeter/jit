# Quick Start Guide

Get up and running with the Java Bytecode JIT Compiler in 5 minutes!

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

Check your versions:
```bash
java -version
mvn -version
```

## Installation

1. Navigate to the project directory:
```bash
cd /Users/peter/workspace/jit
```

2. Build the project:
```bash
mvn clean compile
```

## Running the Demo

### Option 1: Maven exec (Recommended)
```bash
mvn exec:java
```

### Option 2: Packaged JAR
```bash
mvn package
java -jar target/java-bytecode-jit-1.0-SNAPSHOT.jar
```

## Running Tests

```bash
mvn test
```

All 18 tests should pass!

## What You'll See

The demo runs three demonstrations:

### 1. Calculator Class Compilation
- Shows bytecode analysis of math operations
- JIT compiles the class with optimizations
- Executes methods like `add()`, `multiply()`, `factorial()`

### 2. StringProcessor Class Compilation
- Analyzes string manipulation methods
- Compiles and executes string operations
- Tests `reverse()`, `isPalindrome()`, `countVowels()`

### 3. Performance Comparison
- Compares regular execution vs JIT compiled execution
- Demonstrates optimization benefits
- Shows identical results from both approaches

## Sample Output

```
╔═══════════════════════════════════════════════════╗
║   Java Bytecode JIT Compiler Demo                ║
╚═══════════════════════════════════════════════════╝

=== Bytecode Analysis ===
Class: com/jitcompiler/samples/Calculator
Methods:
  add(II)I
    Instructions: 7
    Arithmetic ops: 1

JIT: Optimizing hot method: complexCalculation
✓ Both methods produced identical results!
```

## Key Features

✅ **Bytecode Analysis** - Deep inspection of Java class files  
✅ **Hot Method Detection** - Identifies methods for optimization  
✅ **Constant Folding** - Precomputes constant expressions  
✅ **Dead Code Elimination** - Removes unreachable code  
✅ **Dynamic Loading** - Loads and executes compiled code at runtime  

## Using the JIT Compiler in Your Code

```java
import com.jitcompiler.JitCompiler;

// Create JIT compiler instance
JitCompiler jit = new JitCompiler();

// Compile a class
JitCompiler.CompiledClass compiled = jit.compile("your.package.YourClass");

// Create instance and call methods
Object instance = compiled.newInstance();
Method method = compiled.getMethod("yourMethod", int.class);
int result = (int) method.invoke(instance, 42);
```

## Project Structure

```
jit/
├── pom.xml                          # Maven configuration
├── README.md                        # Full documentation
├── QUICKSTART.md                    # This file
└── src/
    ├── main/java/com/jitcompiler/
    │   ├── BytecodeAnalyzer.java   # Analyzes bytecode
    │   ├── JitCompiler.java        # Main JIT compiler
    │   ├── Main.java               # Demo application
    │   └── samples/                # Sample classes
    └── test/java/com/jitcompiler/  # Test suite
```

## Troubleshooting

### Build Fails
```bash
# Clean and rebuild
mvn clean install
```

### Tests Fail
```bash
# Run with detailed output
mvn test -X
```

### Java Version Issues
```bash
# Check Java version (must be 17+)
java -version

# On macOS with multiple Java versions
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

## Next Steps

1. **Explore the Code**: Read through `JitCompiler.java` to understand the compilation process
2. **Add Your Classes**: Create your own sample classes in `src/main/java/com/jitcompiler/samples/`
3. **Write Tests**: Add tests in `src/test/java/com/jitcompiler/`
4. **Extend Optimizations**: Add new optimization techniques to the compiler

## Learn More

- See [README.md](README.md) for detailed documentation
- Check the test classes for usage examples
- Explore the ASM library documentation: https://asm.ow2.io/

## Help

If you encounter issues:
1. Check that Java 17+ and Maven are installed
2. Verify all files compiled successfully
3. Run `mvn clean compile` to start fresh
4. Check the test output for specific errors

Happy JIT compiling! 🚀

