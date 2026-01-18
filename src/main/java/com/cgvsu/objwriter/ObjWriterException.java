package com.cgvsu.objwriter;

public class ObjWriterException extends RuntimeException {
    private final int lineIndex;

    public ObjWriterException(String message, int lineIndex) {
        super("Error writing OBJ file on line " + lineIndex + ": " + message);
        this.lineIndex = lineIndex;
    }

    public ObjWriterException(String message, int lineIndex, Throwable cause) {
        super("Error writing OBJ file on line " + lineIndex + ": " + message, cause);
        this.lineIndex = lineIndex;
    }

    public int getLineIndex() {
        return lineIndex;
    }
}