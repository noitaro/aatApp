package com.example.myapplication.lua;

import android.util.Log;

import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;

public class CustomDebugLib extends DebugLib {
    private static final String TAG = "CustomDebugLib";
    public boolean interrupted = false;

    @Override
    public void onInstruction(int pc, Varargs v, int top) {
        if (interrupted) {
            throw new ScriptInterruptException();
        }
        super.onInstruction(pc, v, top);
    }

    public static class ScriptInterruptException extends RuntimeException { }
}
