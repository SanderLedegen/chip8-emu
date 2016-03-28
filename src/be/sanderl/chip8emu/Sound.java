package be.sanderl.chip8emu;

import javax.sound.midi.*;

public class Sound {

    private Synthesizer synthesizer;
    private MidiChannel midiChannel;
    private final Chip8Emulator emulator;

    private Runnable soundRunnable = new Runnable() {
        @Override
        public void run() {
            midiChannel.noteOn(60, 120);
            midiChannel.allSoundOff();
        }
    };

    public Sound(Chip8Emulator emulator) {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannel = synthesizer.getChannels()[0];
        } catch (MidiUnavailableException mue) {
            System.err.println(String.format("Sound is not available: %s", mue.getMessage()));
        }

        this.emulator = emulator;
    }

    public void beep() {
        new Thread() {
            @Override
            public void run() {
                midiChannel.noteOn(60, 120);
                try {
                    this.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                midiChannel.allSoundOff();
            }
        }.start();
        emulator.makeBeep = false;
    }
}
