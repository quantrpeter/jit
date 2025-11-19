#!/bin/bash

# Build the Docker image
echo "Building Docker image..."
docker build -t jit-elf-runner .

# Run the calculator executable
echo ""
echo "Running ./calculator in Linux container:"
docker run --rm jit-elf-runner /app/calculator
EXIT_CODE=$?

echo ""
echo "Exit code: $EXIT_CODE"

# Run other executables
echo ""
echo "Running ./simple_expr in Linux container:"
docker run --rm jit-elf-runner /app/simple_expr
echo "Exit code: $?"

echo ""
echo "Running ./calculator_full in Linux container:"
docker run --rm jit-elf-runner /app/calculator_full
echo "Exit code: $?"
