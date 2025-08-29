package com.inhouse.service;

import java.util.ArrayList;
import java.util.List;

import com.inhouse.model.FileChunks;

public class BreakFilesService {

    private static BreakFilesService instance;

    public static BreakFilesService getInstance() {
        if (instance == null) {
            instance = new BreakFilesService();
        }
        return instance;
    }

    public List<FileChunks> breakFiles(List<String> filesInput, long breakPointByteSize) {

        List<FileChunks> filesOutput = new ArrayList<>();

        for (String file: filesInput) {

            System.out.println("Breaking file into smaller parts: " + file);
        
            FileChunks fileChunks = new FileChunks(file);

            // Dummy implementation, replace with actual logic to break file into chunks
            fileChunks.addChunk(0, 1);
            fileChunks.addChunk(2, 3);

            filesOutput.add(fileChunks);
        }

        return filesOutput;
    }

    public void publishFileChuncks(List<FileChunks> files) {
        System.out.println("Publishing file chunks...");

        for (FileChunks fileChunks : files) {
            System.out.println(fileChunks);
        }
    }
}
