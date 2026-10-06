package com.example.macrorecorder;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * Settings menu (default key: I).
 * Deliberately does not override the render method - 26.1 reworked GUI rendering,
 * and plain widgets are drawn by Screen itself, so this stays version-proof.
 */
public class MacroScreen extends Screen {
    private final Screen parent;
    private Button recordingBtn;
    private EditBox commandBox;

    public MacroScreen(Screen parent) {
        super(Component.literal("Macro Recorder"));
        this.parent = parent;
    }

    private static Component onOff(boolean b) { return Component.literal(b ? "ON" : "OFF"); }

    @Override
    protected void init() {
        MacroConfig c = MacroConfig.I;
        int colW = 150, gap = 10, h = 20, step = 24;
        int left = this.width / 2 - colW - gap / 2;
        int right = this.width / 2 + gap / 2;
        int top = Math.max(36, this.height / 2 - 80);

        addRenderableWidget(new StringWidget(this.width / 2 - 100, top - 24, 200, 12, this.title, this.font).alignCenter());
        addRenderableWidget(new StringWidget(left, top - 12, colW, 10, Component.literal("Playback"), this.font));
        addRenderableWidget(new StringWidget(right, top - 12, colW, 10, Component.literal("Safety rule"), this.font));

        // ---- left column: playback ----
        int y = top;
        recordingBtn = addRenderableWidget(Button.builder(recordingLabel(), b -> {
            List<String> all = RecordingStore.list();
            if (!all.isEmpty()) {
                int i = all.indexOf(c.selectedRecording);
                c.selectedRecording = all.get((i + 1) % all.size());
                b.setMessage(recordingLabel());
            }
        }).bounds(left, y, colW - 50, h).build());
        addRenderableWidget(Button.builder(Component.literal("Delete"), b -> {
            if (!c.selectedRecording.isEmpty()) {
                RecordingStore.delete(c.selectedRecording);
                List<String> all = RecordingStore.list();
                c.selectedRecording = all.isEmpty() ? "" : all.get(0);
                recordingBtn.setMessage(recordingLabel());
            }
        }).bounds(left + colW - 46, y, 46, h).build());

        y += step;
        addRenderableWidget(Button.builder(Component.literal("Loop: ").append(onOff(c.loop)), b -> {
            c.loop = !c.loop;
            b.setMessage(Component.literal("Loop: ").append(onOff(c.loop)));
        }).bounds(left, y, colW, h).build());

        y += step;
        addRenderableWidget(new IntSlider(left, y, colW, h, 0, 100, c.loopCount, v -> c.loopCount = v,
            v -> Component.literal(v == 0 ? "Loops: forever" : "Loops: " + v)));

        y += step;
        addRenderableWidget(new IntSlider(left, y, colW, h, 0, 200, c.loopDelayTicks, v -> c.loopDelayTicks = v,
            v -> Component.literal("Delay between loops: " + v + "t")));

        // ---- right column: health rule ----
        y = top;
        addRenderableWidget(Button.builder(Component.literal("Health rule: ").append(onOff(c.healthRuleEnabled)), b -> {
            c.healthRuleEnabled = !c.healthRuleEnabled;
            b.setMessage(Component.literal("Health rule: ").append(onOff(c.healthRuleEnabled)));
        }).bounds(right, y, colW, h).build());

        y += step;
        addRenderableWidget(Button.builder(cmpLabel(), b -> {
            c.triggerWhenBelow = !c.triggerWhenBelow;
            b.setMessage(cmpLabel());
        }).bounds(right, y, colW, h).build());

        y += step;
        addRenderableWidget(new IntSlider(right, y, colW, h, 1, 100, c.healthPercent, v -> c.healthPercent = v,
            v -> Component.literal("Threshold: " + v + "%")));

        y += step;
        addRenderableWidget(Button.builder(actionLabel(), b -> {
            MacroConfig.HealthAction[] all = MacroConfig.HealthAction.values();
            c.healthAction = all[(c.healthAction.ordinal() + 1) % all.length];
            b.setMessage(actionLabel());
        }).bounds(right, y, colW, h).build());

        y += step;
        commandBox = addRenderableWidget(new EditBox(this.font, right, y, colW, h, Component.literal("Command")));
        commandBox.setMaxLength(256);
        commandBox.setHint(Component.literal("/hub"));
        commandBox.setValue(c.command);
        commandBox.setResponder(s -> c.command = s);

        y += step;
        addRenderableWidget(Button.builder(Component.literal("Watch when idle: ").append(onOff(c.monitorWhenNotPlaying)), b -> {
            c.monitorWhenNotPlaying = !c.monitorWhenNotPlaying;
            b.setMessage(Component.literal("Watch when idle: ").append(onOff(c.monitorWhenNotPlaying)));
        }).bounds(right, y, colW, h).build());

        // ---- done ----
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(this.width / 2 - 75, top + step * 6 + 6, 150, h).build());
    }

    private Component recordingLabel() {
        String s = MacroConfig.I.selectedRecording;
        return Component.literal("Recording: " + (s.isEmpty() ? "none" : s.replace("rec-", "")));
    }

    private Component cmpLabel() {
        return Component.literal("Trigger when health: " + (MacroConfig.I.triggerWhenBelow ? "BELOW" : "ABOVE"));
    }

    private Component actionLabel() {
        return Component.literal("Action: " + switch (MacroConfig.I.healthAction) {
            case STOP_ONLY -> "Stop only";
            case RUN_COMMAND -> "Run command";
            case DISCONNECT -> "Leave world/server";
            case COMMAND_THEN_DISCONNECT -> "Command, then leave";
        });
    }

    @Override
    public void removed() { MacroConfig.save(); }

    @Override
    public void onClose() { this.minecraft.setScreen(parent); }

    @Override
    public boolean isPauseScreen() { return false; }

    /** Integer slider. */
    private static final class IntSlider extends AbstractSliderButton {
        private final int min, max;
        private final IntConsumer onChange;
        private final IntFunction<Component> fmt;

        IntSlider(int x, int y, int w, int h, int min, int max, int value, IntConsumer onChange, IntFunction<Component> fmt) {
            super(x, y, w, h, Component.empty(), (value - min) / (double) (max - min));
            this.min = min; this.max = max; this.onChange = onChange; this.fmt = fmt;
            updateMessage();
        }

        private int current() { return min + (int) Math.round(this.value * (max - min)); }

        @Override protected void updateMessage() { setMessage(fmt.apply(current())); }

        @Override protected void applyValue() { onChange.accept(current()); }
    }
}
