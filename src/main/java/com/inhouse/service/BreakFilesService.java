package com.inhouse.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.inhouse.model.FileChunks.Chunk;
import com.inhouse.model.FileChunks;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class BreakFilesService {

    private static volatile BreakFilesService instance;
    private final JedisPool jedisPool;
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_LIST_KEY = "file_chunks_queue";

    /**
     * Private constructor to initialize the Jedis connection pool.
     * Assumes Redis is running on localhost:6379.
     */
    private BreakFilesService() {
        this.jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
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
     * Each message is a JSON string containing the file path, start byte, and end byte.
     * @param files A list of FileChunks objects to be published.
     */
    public void publishFileChuncks(List<FileChunks> files) {
        // Use try-with-resources to automatically return the connection to the pool.
        try (Jedis jedis = jedisPool.getResource()) {
            System.out.println("Publishing file chunks to Redis list '" + REDIS_LIST_KEY + "'...");
            int chunksPublished = 0;

            for (FileChunks file : files) {
                for (Chunk chunk : file.getChunks()) {
                    // Create a JSON message for the worker. Escaping backslashes for Windows paths.
                    String escapedPath = file.getFilePath().replace("\\", "\\\\");
                    String message = String.format(
                        "{\"filePath\": \"%s\", \"startByte\": %d, \"endByte\": %d}",
                        escapedPath,
                        chunk.getStartByte(),
                        chunk.getEndByte()
                    );

                    // LPUSH adds the item to the head of the list. Workers can use RPOP to process in FIFO order.
                    jedis.lpush(REDIS_LIST_KEY, message);
                    System.out.println("Published chunk: " + message);
                    chunksPublished++;
                }
            }
            System.out.println("Successfully published " + chunksPublished + " chunks to Redis.");
        } catch (Exception e) {
            System.err.println("Could not publish to Redis. Is Redis running? Error: " + e.getMessage());
        }
    }

    public void close() {
        jedisPool.close();
    }
}
