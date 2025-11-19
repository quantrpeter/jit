#!/bin/bash
cd /Users/peter/workspace/jit

# Set classpath
CP="target/classes"
for jar in ~/.m2/repository/org/ow2/asm/*/*.jar; do
    CP="$CP:$jar"
done

# Run native compilation demo
java -cp "$CP" com.jitcompiler.NativeMain
