package com.cgvsu.objreader;

public class ObjReaderException extends RuntimeException {
    private final int lineIndex;

    public ObjReaderException(String message, int lineIndex) {
        super("Error parsing OBJ file on line " + lineIndex + ": " + message);
        this.lineIndex = lineIndex;
    }

    public ObjReaderException(String message, int lineIndex, Throwable cause) {
        super("Error parsing OBJ file on line " + lineIndex + ": " + message, cause);
        this.lineIndex = lineIndex;
    }

    public int getLineIndex() {
        return lineIndex;
    }
}