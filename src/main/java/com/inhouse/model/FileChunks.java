package com.inhouse.model;

import java.util.ArrayList;
import java.util.List;

public class FileChunks {

    private String filePath;
    private List<Chunk> chunks;

    public FileChunks(String filePath) {
        this.filePath = filePath;
        this.chunks = new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }   

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void addChunk(Chunk chunk) {
        chunks.add(chunk);
    }

    public void addChunk(long start, long end) {
        Chunk chunk = new Chunk(start, end);
        chunks.add(chunk);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(filePath).append("\n");
        for (Chunk chunk : chunks) {
            sb.append("  Chunk: ").append(chunk.getStartByte()).append(" - ").append(chunk.getEndByte()).append("\n");
        }
        return sb.toString();
    }      

    public static class Chunk {
        private long startByte;
        private long endByte;

        public Chunk(long startByte, long endByte) {
            this.startByte = startByte;
            this.endByte = endByte;
        }

        public long getStartByte() {
            return startByte;
        }

        public long getEndByte() {
            return endByte;
        }
    }
}
