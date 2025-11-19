package com.jitcompiler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * Writes ELF executable files for Linux
 * Supports x86-64 and ARM64 architectures
 */
public class ELFWriter {
    
    // ELF magic numbers
    private static final byte[] ELF_MAGIC = {0x7f, 'E', 'L', 'F'};
    
    // ELF class
    private static final byte ELFCLASS64 = 2;
    
    // ELF data encoding
    private static final byte ELFDATA2LSB = 1;  // Little endian
    
    // ELF version
    private static final byte EV_CURRENT = 1;
    
    // OS/ABI
    private static final byte ELFOSABI_NONE = 0;  // UNIX System V ABI
    
    // Object file type
    private static final short ET_EXEC = 2;  // Executable file
    
    // Machine types
    private static final short EM_X86_64 = 62;
    private static final short EM_AARCH64 = 183;
    
    // Program header types
    private static final int PT_LOAD = 1;
    
    // Program header flags
    private static final int PF_X = 1;  // Execute
    private static final int PF_W = 2;  // Write
    private static final int PF_R = 4;  // Read
    
    private final String architecture;
    private final ByteArrayOutputStream outputStream;
    private final ByteBuffer buffer;
    
    public ELFWriter(String architecture) {
        this.architecture = architecture;
        this.outputStream = new ByteArrayOutputStream();
        this.buffer = ByteBuffer.allocate(4096);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    /**
     * Write an ELF executable file
     */
    public void writeExecutable(Path outputPath, byte[] machineCode, long entryPoint) throws IOException {
        System.out.println("Writing ELF executable: " + outputPath);
        System.out.println("  Architecture: " + architecture);
        System.out.println("  Code size: " + machineCode.length + " bytes");
        
        outputStream.reset();
        
        // ELF addresses
        long baseAddress = 0x400000L;
        long codeAddress = baseAddress + 0x1000;
        
        // Create proper executable wrapper
        // Entry point calls the function and exits with the return value
        byte[] fullCode = wrapWithExit(machineCode);
        
        // Write ELF header
        writeELFHeader(codeAddress + entryPoint, 1);  // 1 program header
        
        // Write program header
        writeProgramHeader(
            PT_LOAD,
            PF_R | PF_X,
            0x1000,  // Offset in file
            codeAddress,  // Virtual address
            fullCode.length,
            fullCode.length,
            0x1000  // Alignment
        );
        
        // Pad to code offset (0x1000)
        while (outputStream.size() < 0x1000) {
            outputStream.write(0);
        }
        
        // Write machine code
        outputStream.write(fullCode);
        
        // Write to file
        Files.write(outputPath, outputStream.toByteArray());
        
        // Make executable
        makeExecutable(outputPath);
        
        System.out.println("  Output size: " + outputStream.size() + " bytes");
        System.out.println("  Entry point: 0x" + Long.toHexString(codeAddress + entryPoint));
        System.out.println("✓ ELF executable written successfully");
    }
    
    /**
     * Wrap machine code with proper _start entry and exit syscall
     */
    private byte[] wrapWithExit(byte[] machineCode) {
        ByteArrayOutputStream wrapped = new ByteArrayOutputStream();
        
        // For x86-64:
        // _start:
        //   call function    ; relative call to the actual function
        //   mov rdi, rax     ; move return value to first argument (exit code)
        //   mov rax, 60      ; syscall number for exit
        //   syscall          ; invoke exit
        // function:
        //   <user's machine code>
        
        if ("X86_64".equals(architecture)) {
            // Wrapper code layout:
            // call rel32       : 5 bytes  (e8 XX XX XX XX)
            // mov rdi, rax     : 3 bytes  (48 89 c7)
            // mov rax, 60      : 7 bytes  (48 c7 c0 3c 00 00 00)
            // syscall          : 2 bytes  (0f 05)
            // Total wrapper: 17 bytes
            // Call offset = 17 - 5 = 12 (skip the remaining 12 bytes after call)
            
            // call +12
            wrapped.write(0xe8);  // call rel32
            int callOffset = 12;  // Skip the remaining wrapper instructions
            wrapped.write(callOffset & 0xFF);
            wrapped.write((callOffset >> 8) & 0xFF);
            wrapped.write((callOffset >> 16) & 0xFF);
            wrapped.write((callOffset >> 24) & 0xFF);
            
            // After call returns:
            // mov rdi, rax (48 89 c7)
            wrapped.write(0x48);
            wrapped.write(0x89);
            wrapped.write(0xc7);
            
            // mov rax, 60 (48 c7 c0 3c 00 00 00)
            wrapped.write(0x48);
            wrapped.write(0xc7);
            wrapped.write(0xc0);
            wrapped.write(0x3c);
            wrapped.write(0x00);
            wrapped.write(0x00);
            wrapped.write(0x00);
            
            // syscall (0f 05)
            wrapped.write(0x0f);
            wrapped.write(0x05);
            
            // User's machine code
            wrapped.write(machineCode, 0, machineCode.length);
        } else {
            // For ARM64 or other architectures, just use the code as-is for now
            wrapped.write(machineCode, 0, machineCode.length);
        }
        
        return wrapped.toByteArray();
    }
    
    private void writeELFHeader(long entryAddress, int phnum) throws IOException {
        buffer.clear();
        
        // ELF identification
        buffer.put(ELF_MAGIC);
        buffer.put(ELFCLASS64);           // 64-bit
        buffer.put(ELFDATA2LSB);          // Little endian
        buffer.put(EV_CURRENT);           // ELF version
        buffer.put(ELFOSABI_NONE);        // UNIX System V ABI
        buffer.put((byte)0);              // ABI version
        
        // Padding (7 bytes)
        for (int i = 0; i < 7; i++) {
            buffer.put((byte)0);
        }
        
        // Object file type
        buffer.putShort(ET_EXEC);
        
        // Machine type
        if ("ARM64".equals(architecture)) {
            buffer.putShort(EM_AARCH64);
        } else {
            buffer.putShort(EM_X86_64);
        }
        
        // ELF version
        buffer.putInt(EV_CURRENT);
        
        // Entry point address
        buffer.putLong(entryAddress);
        
        // Program header offset (right after ELF header)
        buffer.putLong(64);
        
        // Section header offset (none)
        buffer.putLong(0);
        
        // Flags
        buffer.putInt(0);
        
        // ELF header size
        buffer.putShort((short)64);
        
        // Program header entry size
        buffer.putShort((short)56);
        
        // Number of program headers
        buffer.putShort((short)phnum);
        
        // Section header entry size
        buffer.putShort((short)0);
        
        // Number of section headers
        buffer.putShort((short)0);
        
        // Section name string table index
        buffer.putShort((short)0);
        
        outputStream.write(buffer.array(), 0, 64);
    }
    
    private void writeProgramHeader(int type, int flags, long offset, long vaddr,
                                    long filesz, long memsz, long align) throws IOException {
        buffer.clear();
        
        // Segment type
        buffer.putInt(type);
        
        // Segment flags
        buffer.putInt(flags);
        
        // Offset in file
        buffer.putLong(offset);
        
        // Virtual address
        buffer.putLong(vaddr);
        
        // Physical address (same as virtual for user programs)
        buffer.putLong(vaddr);
        
        // Size in file
        buffer.putLong(filesz);
        
        // Size in memory
        buffer.putLong(memsz);
        
        // Alignment
        buffer.putLong(align);
        
        outputStream.write(buffer.array(), 0, 56);
    }
    
    private void makeExecutable(Path path) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        
        try {
            Files.setPosixFilePermissions(path, perms);
        } catch (UnsupportedOperationException e) {
            // Windows doesn't support POSIX permissions
            System.out.println("  Note: Could not set POSIX permissions (not supported on this OS)");
        }
    }
}
