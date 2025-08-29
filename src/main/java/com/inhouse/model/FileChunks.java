package com.inhouse.model;

import java.util.ArrayList;
import java.util.List;

public class FileChunks {

    private String filePath;
    private List<Chunks> chunks;

    public FileChunks(String filePath) {
        this.filePath = filePath;
        this.chunks = new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }   

    public List<Chunks> getChunks() {
        return chunks;
    }

    public void addChunk(Chunks chunk) {
        chunks.add(chunk);
    }

    public void addChunk(long start, long end) {
        Chunks chunk = new Chunks(start, end);
        chunks.add(chunk);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(filePath).append("\n");
        for (Chunks chunk : chunks) {
            sb.append("  Chunk: ").append(chunk.getStart()).append(" - ").append(chunk.getEnd()).append("\n");
        }
        return sb.toString();
    }      

    public static class Chunks {
        private long start;
        private long end;

        public Chunks(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }
}
