package com.inhouse.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import com.inhouse.model.FileChunks.Chunk;
import com.inhouse.dto.FileChunkDTO;
import com.inhouse.model.FileChunks;

public class BreakFilesService {

    private static volatile BreakFilesService instance;
    private final RedissonClient redissonClient;
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_LIST_KEY = "file_chunks_queue";

    private BreakFilesService() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://" + REDIS_HOST + ":" + REDIS_PORT);
        config.setCodec(new JsonJacksonCodec()); 

        this.redissonClient = Redisson.create(config);
    }

    public static BreakFilesService getInstance() {
        // Use double-checked locking for thread-safe singleton initialization.
        BreakFilesService localInstance = instance;
        if (localInstance == null) {
            synchronized (BreakFilesService.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new BreakFilesService();
                }
            }
        }
        return localInstance;
    }

    /**
     * Breaks each file in the input list into smaller chunks based on the specified breakPointByteSize.
     * Each chunk is represented by its start and end byte positions.
     * @param filesInput A list of file paths to be broken into chunks.
     * @param breakPointByteSize The maximum size (in bytes) for each chunk.
     * @return A list of FileChunks objects, each containing the file path and its corresponding chunks.
     */
    public List<FileChunks> breakFiles(List<String> filesInput, long breakPointByteSize) {

        if (breakPointByteSize <= 0) {
            throw new IllegalArgumentException("breakPointByteSize must be positive.");
        }

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

    /**
     * Publishes each file chunk as a separate message to a Redis list.
     * Each message is a JSON containing the file path, start byte, and end byte.
     * @param files A list of FileChunks objects to be published.
     */
    public void publishFileChuncks(List<FileChunks> files) {
        
        RQueue<FileChunkDTO> queue = redissonClient.getQueue(REDIS_LIST_KEY);
        
        System.out.println("Publishing file chunks to Redis list '" + REDIS_LIST_KEY + "'...");
        int chunksPublished = 0;

        for (FileChunks file : files) {
            for (Chunk chunk : file.getChunks()) {

                FileChunkDTO dto = new FileChunkDTO(file.getFilePath(), chunk.getStartByte(), chunk.getEndByte());
                queue.add(dto); 
                
                System.out.println("Published chunk: " + dto.toString());
                chunksPublished++;
            }
        }
        System.out.println("Successfully published " + chunksPublished + " chunks to Redis.");
    }

    public void releaseResources() {
        if (this.redissonClient != null && !this.redissonClient.isShutdown()) {
            this.redissonClient.shutdown();
        }
    }
}
