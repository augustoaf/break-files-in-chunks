package com.inhouse.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        for (String filePath : filesInput) {
            try {
                Path path = Paths.get(filePath);
                long fileSize = Files.size(path);

                // Skip empty files
                if (fileSize == 0) {
                    System.out.println("Skipping empty file: " + filePath);
                    continue;
                }

                System.out.println("Breaking file: " + filePath + " (Size: " + fileSize + " bytes)");
                FileChunks fileChunks = new FileChunks(filePath);

                // Loop through the file, creating chunks based on the breakPointByteSize
                for (long startByte = 0; startByte < fileSize; startByte += breakPointByteSize) {
                    // Calculate the end byte, ensuring it doesn't go past the end of the file
                    long endByte = Math.min(startByte + breakPointByteSize - 1, fileSize - 1);
                    fileChunks.addChunk(startByte, endByte);
                }
                filesOutput.add(fileChunks);

            } catch (IOException e) {
                System.err.println("Error processing file " + filePath + ": " + e.getMessage());
                // Continue to the next file on error
            }
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
