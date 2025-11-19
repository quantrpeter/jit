# Use a minimal Linux image
FROM ubuntu:22.04

# Create working directory
WORKDIR /app

# Copy the ELF executables
COPY output/ /app/

# Make them executable (in case permissions aren't preserved)
RUN chmod +x /app/*

# Default command
CMD ["/bin/bash"]
