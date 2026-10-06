package com.example.macrorecorder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

/** The key mappings that are recorded and replayed. Bit i of a frame's mask = keys()[i]. */
final class Tracked {
    private Tracked() {}

    static KeyMapping[] keys(Options o) {
        return new KeyMapping[] {
            o.keyUp, o.keyDown, o.keyLeft, o.keyRight, o.keyJump, o.keyShift, o.keySprint,
            o.keyAttack, o.keyUse, o.keyDrop, o.keySwapOffhand
        };
    }

    /** Which of the above need a simulated click on the press edge. */
    static final boolean[] NEEDS_CLICK = {
        false, false, false, false, false, false, false,
        true, true, true, true
    };
}
