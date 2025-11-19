#!/bin/bash
cd /Users/peter/workspace/jit

# Make sure code is compiled
mvn compile -q

# Run native compilation demo using maven exec plugin
mvn -q exec:java -Dexec.mainClass="com.jitcompiler.NativeMain" -Dexec.classpathScope=compile
