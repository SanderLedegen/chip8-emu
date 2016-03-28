package be.sanderl.chip8emu.renderer;

import be.sanderl.chip8emu.Chip8Emulator;

import javax.swing.*;
import java.awt.*;

public class AWTRenderer extends JComponent {

    private final Chip8Emulator emulator;
    private final int zoomFactor;

    public AWTRenderer(Chip8Emulator emulator) {
        this(emulator, 10);
    }

    public AWTRenderer(Chip8Emulator emulator, int zoomFactor) {
        this.emulator = emulator;
        this.zoomFactor = zoomFactor;
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics oldGraphics) {
        super.paintComponent(oldGraphics);
        Graphics2D g = (Graphics2D) oldGraphics;

        if (emulator.clearScreen) {
            g.clearRect(0, 0, emulator.NATIVE_SCREEN_WIDTH * zoomFactor, emulator.NATIVE_SCREEN_HEIGHT * zoomFactor);
            emulator.clearScreen = false;
            return;
        }

        byte[] display = emulator.getDisplay();
        for (int ii = 0; ii < display.length; ii++) {
            int x = ii % 64;
            int y = ii / 64;
            g.setColor(display[ii] > 0 ? Color.WHITE : Color.BLACK);
            g.fillRect(x*zoomFactor, y*zoomFactor, zoomFactor, zoomFactor);
        }

        // emulator.clearScreen = false;
        emulator.dirtyGraphics = false;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Chip8Emulator.NATIVE_SCREEN_WIDTH * zoomFactor, Chip8Emulator.NATIVE_SCREEN_HEIGHT * zoomFactor);
    }
}
