package com.jitcompiler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * Writes Mach-O executable files for macOS
 * Supports both x86-64 and ARM64 architectures
 */
public class MachOWriter {
    
    // Mach-O magic numbers
    private static final int MH_MAGIC_64 = 0xfeedfacf;        // 64-bit Mach-O (little endian)
    private static final int MH_CIGAM_64 = 0xcffaedfe;        // 64-bit Mach-O (big endian)
    
    // CPU types
    private static final int CPU_TYPE_X86_64 = 0x01000007;
    private static final int CPU_TYPE_ARM64 = 0x0100000c;
    
    // CPU subtypes
    private static final int CPU_SUBTYPE_X86_64_ALL = 3;
    private static final int CPU_SUBTYPE_ARM64_ALL = 0;
    
    // File types
    private static final int MH_EXECUTE = 0x2;  // Executable file
    
    // Flags
    private static final int MH_NOUNDEFS = 0x1;
    private static final int MH_DYLDLINK = 0x4;
    private static final int MH_PIE = 0x200000;
    
    // Load commands
    private static final int LC_SEGMENT_64 = 0x19;
    private static final int LC_UNIXTHREAD = 0x5;
    private static final int LC_MAIN = 0x80000028;
    
    // Protection flags
    private static final int VM_PROT_READ = 0x1;
    private static final int VM_PROT_WRITE = 0x2;
    private static final int VM_PROT_EXECUTE = 0x4;
    
    private final String architecture;
    private final ByteArrayOutputStream outputStream;
    private final ByteBuffer buffer;
    
    public MachOWriter(String architecture) {
        this.architecture = architecture;
        this.outputStream = new ByteArrayOutputStream();
        this.buffer = ByteBuffer.allocate(4096);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    /**
     * Write a Mach-O executable file
     */
    public void writeExecutable(Path outputPath, byte[] machineCode, long entryPoint) throws IOException {
        System.out.println("Writing Mach-O executable: " + outputPath);
        System.out.println("  Architecture: " + architecture);
        System.out.println("  Code size: " + machineCode.length + " bytes");
        
        outputStream.reset();
        
        // Calculate sizes
        int pageSize = 4096;
        int headerSize = 32;  // mach_header_64
        int segmentCommandSize = 72;  // segment_command_64
        int sectionSize = 80;  // section_64
        int threadCommandSize = "ARM64".equals(architecture) ? 184 : 184;  // thread_command
        
        int loadCommandsSize = segmentCommandSize + sectionSize + threadCommandSize;
        int totalHeaderSize = headerSize + loadCommandsSize;
        int codeOffset = ((totalHeaderSize + pageSize - 1) / pageSize) * pageSize;
        
        // Write Mach-O header
        writeMachHeader(loadCommandsSize, 2);
        
        // Write segment command (__TEXT)
        writeSegmentCommand("__TEXT", codeOffset, machineCode.length, 
                           VM_PROT_READ | VM_PROT_EXECUTE, machineCode.length);
        
        // Write section (__text)
        writeSection("__text", "__TEXT", codeOffset, machineCode.length,
                    VM_PROT_READ | VM_PROT_EXECUTE);
        
        // Write thread/main command
        writeMainCommand(entryPoint + codeOffset);
        
        // Pad to code offset
        while (outputStream.size() < codeOffset) {
            outputStream.write(0);
        }
        
        // Write machine code
        outputStream.write(machineCode);
        
        // Pad to page boundary
        while (outputStream.size() % pageSize != 0) {
            outputStream.write(0);
        }
        
        // Write to file
        Files.write(outputPath, outputStream.toByteArray());
        
        // Make executable
        makeExecutable(outputPath);
        
        System.out.println("  Output size: " + outputStream.size() + " bytes");
        System.out.println("  Entry point: 0x" + Long.toHexString(entryPoint + codeOffset));
        System.out.println("✓ Mach-O executable written successfully");
    }
    
    private void writeMachHeader(int sizeofcmds, int ncmds) throws IOException {
        buffer.clear();
        
        // Magic number
        buffer.putInt(MH_MAGIC_64);
        
        // CPU type and subtype
        if ("ARM64".equals(architecture)) {
            buffer.putInt(CPU_TYPE_ARM64);
            buffer.putInt(CPU_SUBTYPE_ARM64_ALL);
        } else {
            buffer.putInt(CPU_TYPE_X86_64);
            buffer.putInt(CPU_SUBTYPE_X86_64_ALL);
        }
        
        // File type
        buffer.putInt(MH_EXECUTE);
        
        // Number of load commands
        buffer.putInt(ncmds);
        
        // Size of load commands
        buffer.putInt(sizeofcmds);
        
        // Flags
        buffer.putInt(MH_NOUNDEFS | MH_DYLDLINK | MH_PIE);
        
        // Reserved
        buffer.putInt(0);
        
        outputStream.write(buffer.array(), 0, 32);
    }
    
    private void writeSegmentCommand(String segname, long fileoff, long filesize,
                                     int maxprot, long vmsize) throws IOException {
        buffer.clear();
        
        // Command type
        buffer.putInt(LC_SEGMENT_64);
        
        // Command size (includes section)
        buffer.putInt(72 + 80);  // segment_command_64 + section_64
        
        // Segment name (16 bytes, null-padded)
        writeString(segname, 16);
        
        // VM address
        buffer.putLong(0x100000000L);
        
        // VM size
        buffer.putLong(vmsize);
        
        // File offset
        buffer.putLong(fileoff);
        
        // File size
        buffer.putLong(filesize);
        
        // Max protection
        buffer.putInt(maxprot);
        
        // Initial protection
        buffer.putInt(maxprot);
        
        // Number of sections
        buffer.putInt(1);
        
        // Flags
        buffer.putInt(0);
        
        outputStream.write(buffer.array(), 0, 72);
    }
    
    private void writeSection(String sectname, String segname, long offset, long size,
                             int flags) throws IOException {
        buffer.clear();
        
        // Section name (16 bytes, null-padded)
        writeString(sectname, 16);
        
        // Segment name (16 bytes, null-padded)
        writeString(segname, 16);
        
        // Address
        buffer.putLong(0x100000000L + offset);
        
        // Size
        buffer.putLong(size);
        
        // Offset
        buffer.putInt((int)offset);
        
        // Alignment
        buffer.putInt(4);  // 2^4 = 16 byte alignment
        
        // Relocations offset
        buffer.putInt(0);
        
        // Number of relocations
        buffer.putInt(0);
        
        // Flags
        buffer.putInt(0x80000400);  // S_ATTR_PURE_INSTRUCTIONS | S_ATTR_SOME_INSTRUCTIONS
        
        // Reserved
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        
        outputStream.write(buffer.array(), 0, 80);
    }
    
    private void writeMainCommand(long entrypoint) throws IOException {
        buffer.clear();
        
        // Command type
        buffer.putInt(LC_MAIN);
        
        // Command size
        buffer.putInt(24);
        
        // Entry offset
        buffer.putLong(entrypoint - 0x100000000L);
        
        // Stack size
        buffer.putLong(0);
        
        outputStream.write(buffer.array(), 0, 24);
    }
    
    private void writeString(String str, int length) {
        byte[] bytes = str.getBytes();
        for (int i = 0; i < length; i++) {
            if (i < bytes.length) {
                buffer.put(bytes[i]);
            } else {
                buffer.put((byte) 0);
            }
        }
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
            // Fallback for non-POSIX systems
            path.toFile().setExecutable(true, false);
        }
    }
}

