package com.jitcompiler;

import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates native machine code from Java bytecode
 * Supports x86-64 and ARM64 (Apple Silicon) architectures
 */
public class MachineCodeGenerator {
    
    private final String architecture;
    private final List<Byte> machineCode;
    private final Map<String, Integer> labelPositions;
    private int codeOffset;
    
    public MachineCodeGenerator() {
        this.architecture = detectArchitecture();
        this.machineCode = new ArrayList<>();
        this.labelPositions = new HashMap<>();
        this.codeOffset = 0;
        
        System.out.println("Machine Code Generator initialized for: " + architecture);
    }
    
    private String detectArchitecture() {
        String osArch = System.getProperty("os.arch").toLowerCase();
        if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            return "ARM64";
        } else if (osArch.contains("x86_64") || osArch.contains("amd64")) {
            return "X86_64";
        }
        return "X86_64"; // default
    }
    
    /**
     * Generate machine code for a method
     */
    public byte[] generateMethodCode(MethodNode method) {
        machineCode.clear();
        labelPositions.clear();
        codeOffset = 0;
        
        System.out.println("  Generating native code for method: " + method.name);
        
        // Generate prologue
        generatePrologue();
        
        // Generate code for each instruction
        if (method.instructions != null) {
            for (AbstractInsnNode insn : method.instructions) {
                generateInstruction(insn);
            }
        }
        
        // Generate epilogue
        generateEpilogue();
        
        // Convert to byte array
        byte[] code = new byte[machineCode.size()];
        for (int i = 0; i < machineCode.size(); i++) {
            code[i] = machineCode.get(i);
        }
        
        System.out.println("  Generated " + code.length + " bytes of native code");
        return code;
    }
    
    private void generatePrologue() {
        if ("ARM64".equals(architecture)) {
            generateARM64Prologue();
        } else {
            generateX86_64Prologue();
        }
    }
    
    private void generateEpilogue() {
        if ("ARM64".equals(architecture)) {
            generateARM64Epilogue();
        } else {
            generateX86_64Epilogue();
        }
    }
    
    private void generateInstruction(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        
        switch (opcode) {
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                int value = opcode - ICONST_0;
                generatePushConstant(value);
                break;
                
            case BIPUSH:
                IntInsnNode bipush = (IntInsnNode) insn;
                generatePushConstant(bipush.operand);
                break;
                
            case LDC:
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst instanceof Integer) {
                    generatePushConstant((Integer) ldc.cst);
                }
                break;
                
            case IADD:
                generateAdd();
                break;
                
            case ISUB:
                generateSub();
                break;
                
            case IMUL:
                generateMul();
                break;
                
            case IDIV:
                generateDiv();
                break;
                
            case IRETURN:
                // Return value already in accumulator
                break;
                
            case RETURN:
                // Nothing to return
                break;
                
            case ILOAD:
            case 26:  // ILOAD_0
            case 27:  // ILOAD_1
            case 28:  // ILOAD_2
            case 29:  // ILOAD_3
                int loadIndex = getVarIndex(insn, opcode, 26);
                generateLoadLocal(loadIndex);
                break;
                
            case ISTORE:
            case 59:  // ISTORE_0
            case 60:  // ISTORE_1
            case 61:  // ISTORE_2
            case 62:  // ISTORE_3
                int storeIndex = getVarIndex(insn, opcode, 59);
                generateStoreLocal(storeIndex);
                break;
                
            default:
                // Unsupported instruction - generate NOP
                generateNop();
        }
    }
    
    private int getVarIndex(AbstractInsnNode insn, int opcode, int baseOpcode) {
        if (insn instanceof VarInsnNode) {
            return ((VarInsnNode) insn).var;
        } else {
            return opcode - baseOpcode;
        }
    }
    
    // ===== X86-64 Code Generation =====
    
    private void generateX86_64Prologue() {
        // push rbp
        emit(0x55);
        // mov rbp, rsp
        emit(0x48, 0x89, 0xe5);
        // sub rsp, 64 (allocate stack space)
        emit(0x48, 0x83, 0xec, 0x40);
    }
    
    private void generateX86_64Epilogue() {
        // mov rsp, rbp
        emit(0x48, 0x89, 0xec);
        // pop rbp
        emit(0x5d);
        // ret
        emit(0xc3);
    }
    
    private void generatePushConstant(int value) {
        if ("ARM64".equals(architecture)) {
            // mov w0, #value (simplified - actual encoding is more complex)
            // For now, we'll use a simplified approach
            emit(0x52, 0x80, (byte)((value & 0xFF) << 5), (byte)(value & 0x1F));
        } else {
            // mov eax, immediate
            emit(0xb8);
            emitInt32(value);
            // push rax
            emit(0x50);
        }
    }
    
    private void generateAdd() {
        if ("ARM64".equals(architecture)) {
            // add w0, w1, w2 (simplified)
            emit(0x0b, 0x02, 0x00, 0x00);
        } else {
            // pop rbx
            emit(0x5b);
            // pop rax
            emit(0x58);
            // add eax, ebx
            emit(0x01, 0xd8);
            // push rax
            emit(0x50);
        }
    }
    
    private void generateSub() {
        if ("ARM64".equals(architecture)) {
            // sub w0, w1, w2
            emit(0x4b, 0x02, 0x00, 0x00);
        } else {
            // pop rbx
            emit(0x5b);
            // pop rax
            emit(0x58);
            // sub eax, ebx
            emit(0x29, 0xd8);
            // push rax
            emit(0x50);
        }
    }
    
    private void generateMul() {
        if ("ARM64".equals(architecture)) {
            // mul w0, w1, w2
            emit(0x1b, 0x02, 0x7c, 0x00);
        } else {
            // pop rbx
            emit(0x5b);
            // pop rax
            emit(0x58);
            // imul eax, ebx
            emit(0x0f, 0xaf, 0xc3);
            // push rax
            emit(0x50);
        }
    }
    
    private void generateDiv() {
        if ("ARM64".equals(architecture)) {
            // sdiv w0, w1, w2
            emit(0x1a, 0xc2, 0x0c, 0x00);
        } else {
            // pop rbx
            emit(0x5b);
            // pop rax
            emit(0x58);
            // xor edx, edx
            emit(0x31, 0xd2);
            // idiv ebx
            emit(0xf7, 0xfb);
            // push rax
            emit(0x50);
        }
    }
    
    private void generateLoadLocal(int index) {
        if ("ARM64".equals(architecture)) {
            // ldr w0, [sp, #(index*4)]
            int offset = index * 4;
            emit(0xb9, 0x40 | ((offset >> 2) & 0x3f), 0x00, 0xe0);
        } else {
            // mov eax, [rbp - offset]
            int offset = (index + 1) * 4;
            emit(0x8b, 0x45, (byte)(-offset));
            // push rax
            emit(0x50);
        }
    }
    
    private void generateStoreLocal(int index) {
        if ("ARM64".equals(architecture)) {
            // str w0, [sp, #(index*4)]
            int offset = index * 4;
            emit(0xb9, 0x00 | ((offset >> 2) & 0x3f), 0x00, 0xe0);
        } else {
            // pop rax
            emit(0x58);
            // mov [rbp - offset], eax
            int offset = (index + 1) * 4;
            emit(0x89, 0x45, (byte)(-offset));
        }
    }
    
    private void generateNop() {
        if ("ARM64".equals(architecture)) {
            emit(0x1f, 0x20, 0x03, 0xd5); // nop
        } else {
            emit(0x90); // nop
        }
    }
    
    // ===== ARM64 Code Generation =====
    
    private void generateARM64Prologue() {
        // stp x29, x30, [sp, #-16]!
        emit(0xfd, 0x7b, 0xbf, 0xa9);
        // mov x29, sp
        emit(0xfd, 0x03, 0x00, 0x91);
        // sub sp, sp, #64
        emit(0xff, 0x43, 0x01, 0xd1);
    }
    
    private void generateARM64Epilogue() {
        // add sp, sp, #64
        emit(0xff, 0x43, 0x01, 0x91);
        // ldp x29, x30, [sp], #16
        emit(0xfd, 0x7b, 0xc1, 0xa8);
        // ret
        emit(0xc0, 0x03, 0x5f, 0xd6);
    }
    
    // ===== Helper Methods =====
    
    private void emit(int... bytes) {
        for (int b : bytes) {
            machineCode.add((byte) b);
            codeOffset++;
        }
    }
    
    private void emitInt32(int value) {
        machineCode.add((byte) (value & 0xFF));
        machineCode.add((byte) ((value >> 8) & 0xFF));
        machineCode.add((byte) ((value >> 16) & 0xFF));
        machineCode.add((byte) ((value >> 24) & 0xFF));
        codeOffset += 4;
    }
    
    public String getArchitecture() {
        return architecture;
    }
}

