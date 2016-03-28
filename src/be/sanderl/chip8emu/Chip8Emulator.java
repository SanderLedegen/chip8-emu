package be.sanderl.chip8emu;

public class Chip8Emulator {

    private static final byte INSTRUCTION_SIZE = 2;
    public static final byte NATIVE_SCREEN_WIDTH = 64;
    public static final byte NATIVE_SCREEN_HEIGHT = 32;
    private static final char[] FONT_SET = new char[] {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    // Implementation-specific flags
    public boolean dirtyGraphics;
    public boolean clearScreen;
    public boolean makeBeep;
    public int instruction;
    public boolean debugEnabled;
    public boolean debugStepRequested;

    // 4kB 8-bit memory
    private byte[] memory;

    // 16 8-bit registers (also called V)
    private byte[] V;

    // Address register consists of 16 bits
    private char I;

    // Program counter (0x000 -> 0xFFF; equals memory size)
    private char pc;

    // 16-bit stack with 16 levels of nested subroutines.
    private char[] stack;

    // 8-bit stack pointer
    private byte sp;

    // 60 hertz count-down timers
    private byte delayTimer;
    private byte soundTimer;

    // 16-key keyboard
    private boolean[] input;

    // Monochrome, 64x32 pixels resolution
    private byte[] display;

    public void init() {
        dirtyGraphics = false;
        clearScreen = false;

        memory = new byte[4096];
        V = new byte[16];
        I = 0;
        pc = 0x200; // Programs start at this location
        stack = new char[16];
        sp = 0;
        delayTimer = 0;
        soundTimer = 0;
        input = new boolean[16];
        display = new byte[NATIVE_SCREEN_HEIGHT * NATIVE_SCREEN_WIDTH];

        loadFontSet();
    }

    public void runCycle() {
        /*
        Instructions are two bytes long, a memory location is 1 byte.
        Fetch two successive instruction parts, bit-shift the first byte to the left
        and append the second one to construct the complete instruction.
         */
        instruction = (memory[pc] << 8 | (memory[pc + 1] & 0xFF));
        pc += INSTRUCTION_SIZE;

        executeInstruction();
        updateTimers();
    }

    private void executeInstruction() {

        switch (instruction & 0xF000) {
            case 0x0000:
                switch (instruction & 0x00FF) {
                    case 0xE0: // 00E0 - CLS; Clear display
                        clearScreen = true;
                        display = new byte[NATIVE_SCREEN_HEIGHT * NATIVE_SCREEN_WIDTH];
                        break;
                    case 0xEE: // 00EE - RET; Return from subroutine
                        sp--;
                        pc = stack[sp];
                        break;
                    default:
                        System.err.println(String.format("Unknown instruction %04x", instruction));
                }
                break;

            case 0x1000: // 1nnn - JP addr; Jump to location nnn
                pc = (char) (instruction & 0x0FFF);
                break;

            case 0x2000: // 2nnn - CALL addr; Call subroutine at nnn
                stack[sp] = pc;
                sp++;
                pc = (char) (instruction & 0x0FFF);
                break;

            case 0x3000: { // 3xkk - SE Vx, byte; Skip next instruction if Vx = kk
                int x = (instruction & 0x0F00) >> 8;
                if (V[x] == (instruction & 0x00FF)) {
                    pc += INSTRUCTION_SIZE;
                }
                break;
            }

            case 0x4000: { // 4xkk - SNE Vx, byte; Skip next instruction if Vx != kk
                int x = (instruction & 0x0F00) >> 8;
                if (V[x] != (instruction & 0x00FF)) {
                    pc += INSTRUCTION_SIZE;
                }
                break;
            }

            case 0x5000: { // 5xy0 - SE Vx, Vy; Skip next instruction if Vx = Vy
                int x = (instruction & 0x0F00) >> 8;
                int y = (instruction & 0x00F0) >> 4;
                if (V[x] == V[y]) {
                    pc += INSTRUCTION_SIZE;
                }
                break;
            }

            case 0x6000: { // 6xkk - LD Vx, byte; Put value kk into register Vx
                int x = (instruction & 0x0F00) >> 8;
                V[x] = (byte) (instruction & 0x00FF);
                break;
            }

            case 0x7000: { // 7xkk - ADD Vx, byte; Add value kk to value of register Vx, store result in Vx
                int x = (instruction & 0x0F00) >> 8;
                V[x] += (byte) instruction & 0x00FF;
                break;
            }

            case 0x8000: {
                int x = (instruction & 0x0F00) >> 8;
                int y = (instruction & 0x00F0) >> 4;

                switch (instruction & 0x000F) {
                    case 0x0: // 8xy0 - LD Vx, Vy; Set Vx = Vy
                        V[x] = V[y];
                        break;

                    case 0x1: // 8xy1 - OR Vx, Vy; Set Vx OR Vy
                        V[x] |= V[y];
                        break;

                    case 0x2: // 8xy2 - AND Vx, Vy; Set Vx AND Vy
                        V[x] &= V[y];
                        break;

                    case 0x3: // 8xy3 - XOR Vx, Vy; Set Vx XOR Vy
                        V[x] ^= V[y];
                        break;

                    case 0x4: // 8xy4 - ADD Vx, Vy; Set Vx += Vy, set VF = carry
                        // TODO: I can't just add these together (signed <-> unsigned), can I?
                        V[x] += V[y];
                        V[0xF] = (byte) (((int) V[x] + (int) V[y]) > 0xFF ? 1 : 0);
                        break;

                    case 0x5: // 8xy5 - SUB Vx, Vy; Set Vx -= Vy, set VF = !carry
                        V[0xF] = (byte) (V[x] > V[y] ? 1 : 0);
                        V[x] -= V[y];
                        break;

                    case 0x6: // 8xy6 - SHR Vx {, Vy}; Set Vx = Vx SHR 1
                        V[0xF] = (byte) (V[x] & 0x1);
                        V[x] = (byte) (V[x] >> 1); // Divide by 2
                        break;

                    case 0x7: // 8xy7 - SUBN Vx, Vy; Set Vx = Vy - Vx, set VF = !carry
                        V[0xF] = (byte) (V[y] > V[x] ? 1 : 0);
                        V[x] = (byte) (V[y] - V[x]);
                        break;

                    case 0xE: // 8xyE - SHL Vx {, Vy}; Set Vx = Vx SHL 1
                        V[0xF] = (byte) ((V[x] & 0x80) >> 7);
                        V[x] = (byte) (V[x] << 1); // Multiply by 2
                        break;

                    default:
                        System.err.println(String.format("Unknown instruction %04x", instruction));
                }

                break;
            }

            case 0x9000: { // 9xy0 - SNE Vx, Vy; Skip next instruction if Vx != Vy
                int x = (instruction & 0x0F00) >> 8;
                int y = (instruction & 0x00F0) >> 4;
                if (V[x] != V[y]) {
                    pc += INSTRUCTION_SIZE;
                }
                break;
            }

            case 0xA000: // Annn - LD I, addr; Set I = nnn
                I = (char) (instruction & 0x0FFF);
                break;

            case 0xB000: // Bnnn - JP V0, addr; Jump to location nnn + V0
                pc = (char) (instruction & 0x0FFF + V[0]);
                break;

            case 0xC000: { // Cxkk - RND Vx, byte; Set Vx = random byte AND kk
                int x = (instruction & 0x0F00) >> 8;
                int kk = instruction & 0x00FF;
                byte rand = (byte) Math.floor(Math.random() * 256);
                V[x] = (byte) (rand & kk);
                break;
            }

            case 0xD000: { // Dxyn - DRW Vx, Vy, nibble; Display n-byte sprite at memory location I at (Vx, Vy), set VF = collision
                int x = V[(instruction & 0x0F00) >> 8];
                int y = V[(instruction & 0x00F0) >> 4];
                int n = instruction & 0xF;

                for (int ii = 0; ii < n; ii++) {
                    byte singleSpriteLine = memory[I + ii];

                    for (int jj = 0; jj < 8; jj++) {
                        int pixel = singleSpriteLine & (0x80 >> jj);
                        if (pixel > 0) {
                            int displayPointer = x + jj + (y + ii) * NATIVE_SCREEN_WIDTH;
                            V[0xF] = display[displayPointer] == 1 ? (byte) 1 : 0;
                            display[displayPointer] ^= 1;
                        }
                    }
                }

                dirtyGraphics = true;
                break;
            }

            case 0xE000:
                switch (instruction & 0x00FF) {
                    case 0x9E: { // Ex9E - SKP Vx; Skip next instruction if key with value Vx is pressed
                        int x = (instruction & 0x0F00) >> 8;
                        if (input[V[x]]) {
                            pc += INSTRUCTION_SIZE;
                        }
                        break;
                    }

                    case 0xA1: { // ExA1 - SKNP Vx; Skip next instruction if key with value Vx is not pressed
                        int x = (instruction & 0x0F00) >> 8;
                        if (!input[V[x]]) {
                            pc += INSTRUCTION_SIZE;
                        }
                        break;
                    }

                    default:
                        System.err.println(String.format("Unknown instruction %04x", instruction));
                }
                break;

            case 0xF000: {
                int x = (instruction & 0x0F00) >> 8;
                switch (instruction & 0x00FF) {
                    case 0x7: // Fx07 - LD Vx, DT; Set Vx = delay timer
                        V[x] = delayTimer;
                        break;

                    case 0xA: // Fx0A - LD Vx, K; Wait for key, store value of key in Vx
                        for (int ii = 0; ii < input.length; ii++) {
                            if (input[ii]) {
                                V[x] = (byte) ii;
                                break;
                            }
                        }
                        break;

                    case 0x15: // Fx15 - LD DT, Vx; Set delay timer = Vx
                        delayTimer = V[x];
                        break;

                    case 0x18: // Fx18 - LD ST, Vx; Set sound timer = Vx
                        soundTimer = V[x];
                        break;

                    case 0x1E: // Fx1E - ADD I, Vx; Set I += Vx.
                        I += V[x];
                        break;

                    case 0x29: // Fx29 - LD F, Vx; Set I = sprite location for digit Vx.
                        I = (char) (5 * V[x]);
                        break;

                    case 0x33: // Fx33 - LD B, Vx; Store hundreds/tens/ones of decimal value of Vx in I, I+1 and I+2.
                        memory[I] = (byte) ((V[x] & 0xFF) / 100);
                        memory[I + 1] = (byte) (((V[x] & 0xFF) % 100) / 10);
                        memory[I + 2] = (byte) (((V[x] & 0xFF) % 100) % 10);
                        break;

                    case 0x55: // Fx55 - LD [I], Vx; Store registers V0 to Vx in memory starting at location I.
                        for (int ii = 0; ii <= x; ii++) {
                            memory[I + ii] = V[ii];
                        }
                        break;

                    case 0x65: // Fx55 - LD [I], Vx; Read registers V0 to Vx from memory starting at location I.
                        for (int ii = 0; ii <= x; ii++) {
                            V[ii] = memory[I + ii];
                        }
                        break;
                }
                break;
            }

            default:
                System.err.println(String.format("Unknown instruction %04x", instruction));
        }
    }

    private void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
            if (soundTimer > 0) {
                makeBeep = true;
            }
        }
    }

    private void loadFontSet() {
        // The font set is located at the beginning of the memory.
        for (int ii = 0; ii < FONT_SET.length; ii++) {
            memory[ii] = (byte) FONT_SET[ii];
        }
    }

    public void loadProgram(byte[] program) {
        // Programs are loaded at memory location 0x200.
        for (int ii = 0; ii < program.length; ii++) {
            memory[ii + 0x200] = program[ii];
        }
    }

    public byte[] getDisplay() {
        return display;
    }

    public void setInput(int key, boolean isPressed) {
        input[key] = isPressed;
    }

    public void debug() {
        System.out.print(String.format("Instruction: %04X\t", instruction & 0xFFFF));
        System.out.println(String.format("PC: %04X", (int) pc));
        for (int ii = 0; ii < 16; ii+=4) {
            System.out.print(String.format("V%x: %02X\t", ii, V[ii] & 0xFF));
            System.out.print(String.format("V%x: %02X\t", ii + 1, V[ii+1] & 0xFF));
            System.out.print(String.format("V%x: %02X\t", ii + 2, V[ii+2] & 0xFF));
            System.out.print(String.format("V%x: %02X\n", ii + 3, V[ii+3] & 0xFF));
        }
        System.out.print(String.format("I: %04X\t", (int) I));
        System.out.print(String.format("SP: %04X\t", (int) sp));
        System.out.print(String.format("DT: %02X\t", (int) delayTimer));
        System.out.println(String.format("ST: %02X", (int) soundTimer));
    }
}
