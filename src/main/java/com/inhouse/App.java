package com.inhouse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.inhouse.model.FileChunks;
import com.inhouse.service.BreakFilesService;

public class App 
{

    private static String folderToScan = "D:/repos/handle-files-in-scale/breakfiles/data/input";
    
    public static void main( String[] args )
    {

        BreakFilesService breakFilesService = BreakFilesService.getInstance();

        // list all files to be broken
        List<String> files = getFiles();
        
        // break files into smaller chunks
        List<FileChunks> filesOutput = breakFilesService.breakFiles(files, 1024);
        
        // publish file chunks to a message broker or any other system for further processing
        breakFilesService.publishFileChuncks(filesOutput);
        
        System.out.println( "completed!" );
    }

    private static List<String> getFiles() {
        // Define the folder to scan.
        Path dir = Paths.get(folderToScan);

        if (!Files.isDirectory(dir)) {
            System.err.println("Directory not found: " + dir.toAbsolutePath());
            return Collections.emptyList();
        }

        // Use try-with-resources to ensure the stream is closed, preventing resource leaks.
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(file -> !Files.isDirectory(file)) // Ensure it's a file, not a subdirectory.
                    .map(Path::toString) // Convert the Path object to its String representation.
                    .filter(fileName -> fileName.endsWith(".txt")) // Filter for files ending with .txt.
                    .collect(Collectors.toList()); // Collect the results into a List.
        } catch (IOException e) {
            System.err.println("Error reading files from directory: " + dir.toAbsolutePath());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
