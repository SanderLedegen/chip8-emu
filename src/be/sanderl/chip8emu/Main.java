package be.sanderl.chip8emu;

import be.sanderl.chip8emu.renderer.AWTRenderer;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        try {
            new Main();
        } catch (Exception e) {
            System.err.println("An error occurred while emulating.");
            System.err.println(e.getMessage());
        }
    }

    public Main() {

        // Initialize Chip-8 emulator
        Chip8Emulator emulator = new Chip8Emulator();
        emulator.init();
        emulator.loadProgram(Main.readProgramFile("breakout.ch8"));
        // emulator.debugEnabled = true;

        // Set up graphics, sound and input
        AWTRenderer renderer = new AWTRenderer(emulator, 10);
        Input input = new Input(emulator);
        Sound sound = new Sound(emulator);
        createWindow(renderer, input);

        // May the gods be in our favor; let's kick off the emulation!
        while (true) {

            if (emulator.debugEnabled) {
                emulator.debug();
                while (!emulator.debugStepRequested && emulator.debugEnabled) {
                    sleep(50);
                }
                emulator.debugStepRequested = false;
            }

            emulator.runCycle();

            if (emulator.dirtyGraphics || emulator.clearScreen) {
                renderer.repaint();
            }

            if (emulator.makeBeep) {
                sound.beep();
            }

            sleep(17);
        }
    }

    private static byte[] readProgramFile(String fileName) {
        byte[] program = null;
        Path path = Paths.get(fileName);

        try {
            program = Files.readAllBytes(path);
        } catch (IOException ioe) {
            System.err.println(String.format("Could not read program located at '%s'.", path.toAbsolutePath()));
            System.exit(1);
        }

        return program;
    }

    private JFrame createWindow(JComponent component, Input input) {
        JFrame frame = new JFrame();
        frame.add(component);
        frame.pack();
        frame.addKeyListener(input);
        frame.setTitle("Chip-8 emulator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            // careCup.isEmpty() -> true
        }
    }
}
