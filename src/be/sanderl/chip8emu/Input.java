package be.sanderl.chip8emu;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class Input implements KeyListener {

    private final Chip8Emulator emulator;
    private final Map<Integer, Integer> keyMapping;

    public Input(Chip8Emulator emulator) {
        this.emulator = emulator;
        keyMapping = new HashMap<>(16);
        keyMapping.put(KeyEvent.VK_NUMPAD0, 0);
        keyMapping.put(KeyEvent.VK_NUMPAD1, 0x1);
        keyMapping.put(KeyEvent.VK_NUMPAD2, 0x2);
        keyMapping.put(KeyEvent.VK_NUMPAD3, 0x3);
        keyMapping.put(KeyEvent.VK_NUMPAD4, 0x4);
        keyMapping.put(KeyEvent.VK_NUMPAD5, 0x5);
        keyMapping.put(KeyEvent.VK_NUMPAD6, 0x6);
        keyMapping.put(KeyEvent.VK_NUMPAD7, 0x7);
        keyMapping.put(KeyEvent.VK_NUMPAD8, 0x8);
        keyMapping.put(KeyEvent.VK_NUMPAD9, 0x9);
        keyMapping.put(KeyEvent.VK_A, 0xA);
        keyMapping.put(KeyEvent.VK_B, 0xB);
        keyMapping.put(KeyEvent.VK_C, 0xC);
        keyMapping.put(KeyEvent.VK_D, 0xD);
        keyMapping.put(KeyEvent.VK_E, 0xE);
        keyMapping.put(KeyEvent.VK_F, 0xF);
    }

    private void handleKey(int keyCode, boolean pressed) {
        Integer lookup = keyMapping.get(keyCode);

        if (lookup != null) {
            emulator.setInput(lookup, pressed);
        } else {
            if (keyCode == KeyEvent.VK_F12) {
                emulator.debugEnabled = !emulator.debugEnabled;
            } else if (keyCode == KeyEvent.VK_F8 && emulator.debugEnabled) {
                emulator.debugStepRequested = true;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handleKey(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
