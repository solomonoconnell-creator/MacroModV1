package com.example.macrorecorder;

/** One entry per client tick (20/s). Stored as parallel arrays so the JSON stays small. */
public class Recording {
    public int version = 1;
    public int[] keys = new int[0];
    public float[] yaw = new float[0];
    public float[] pitch = new float[0];
    public int[] slot = new int[0];

    public int length() { return keys.length; }
}
