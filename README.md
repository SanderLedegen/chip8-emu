# Chip-8 emulator
## Description
This project consists of an emulator designed to run programs written in the Chip-8 language. The focus of this project was mainly to grasp some of the interesting concepts of emulating a platform and all of its underlying systems.

## Usage
Using the emulator can be a little tricky because it's still in a rudimentary state. Loading programs, adjusting the speed of emulation and so on, cannot be done through (external) configuration at the moment. You have to fiddle with the ``Main.java`` class to do so.

## Input
Chip-8 programs expect a 16-key input device; a so-called hex keypad. As I haven't spent much time trying to come up with a suitable keyboard layout, I've just mapped the keys as-is. This means that Numpad 0 to 9 and the keys A to F will be your best friend to control this emulator. If you would like to change a key mapping, take a look at ``Input.java``.

## Limitations
First of all, all opcodes have been implemented and the screen is working so you can get feedback from the system. Because I only tested a handful of programs, it is almost guaranteed some bugs are still present while the performance could also be further improved. Sound has been carried out by making use of Java's MIDI capabilities but could definitely use a nicer implementation as well. A lot of buts and ifs, although it is possible to run some oldskool games :wink:

## Screenshots
![Airplane](https://github.com/SanderLedegen/chip8-emu/raw/master/chip8_airplane.png)
![Breakout](https://github.com/SanderLedegen/chip8-emu/raw/master/chip8_breakout.png)
![Tetris](https://github.com/SanderLedegen/chip8-emu/raw/master/chip8_tetris.png)
