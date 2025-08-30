# Break Files

A Java program to break down large files into smaller, manageable chunks and publish their metadata to a Redis queue. This is the first step in a scalable, distributed file processing pipeline.

## Overview

This project addresses the challenge of processing very large files by dividing the work. It doesn't split the files on disk; instead, it reads the file sizes and calculates byte ranges (chunks). For each chunk, it creates a metadata message containing the original file path, a start byte, and an end byte. These messages are then published to a Redis list, which can be consumed by a pool of worker applications to process parts of the files in parallel.

## Features

-   **File Chunking**: Splits files into logical chunks based on a specified byte size.
-   **Redis Integration**: Publishes chunk metadata to a Redis list using the Redisson client.
-   **JSON-based Messaging**: Serializes chunk data into JSON format

## Prerequisites

-   Java 11 or higher
-   Apache Maven
-   A running Redis instance

## How to Run

1.  **Clone the repository:**
    ```sh
    git clone <repository-url>
    cd breakfiles
    ```

2.  **Place files for processing:**
    Create an `inputs` directory in the project root and place the large files you want to process inside it.
    ```sh
    mkdir inputs
    cp /path/to/your/largefile.dat inputs/
    ```

3.  **Configure Redis (Optional):**
    By default, the application tries to connect to Redis at `localhost:6379`. If your Redis instance is running elsewhere, you can modify the connection details in `src/main/java/com/inhouse/service/BreakFilesService.java`:
    ```java
    private static final String REDIS_HOST = "your-redis-host";
    private static final int REDIS_PORT = 6379;
    ```

4.  **Build the project:**
    ```sh
    mvn clean package
    ```

5.  **Run the application:**
    ```sh
    java -jar target/breakfiles-1.0-SNAPSHOT.jar
    ```
    *(Note: The exact JAR file name may vary based on the version in `pom.xml`)*

## How It Works

The application performs the following steps:

1.  **Initialization**: It starts up and initializes the `BreakFilesService`, which establishes a connection to Redis.
2.  **File Discovery**: It scans the `./inputs` directory for files to process.
3.  **Chunk Calculation**: For each file, it iterates based on a predefined chunk size (e.g., 1MB). It calculates the `startByte` and `endByte` for each chunk. Empty files are skipped.
4.  **Message Publishing**: Each calculated chunk is encapsulated in a `FileChunkDTO` object. This object is then serialized to JSON and pushed (`RPUSH`) into a Redis list named `file_chunks_queue`.
5.  **Completion**: Once all chunks from all files are published, the application prints a summary and shuts down the Redis connection.

### Redis Queue Message Format

A worker consuming from the `file_chunks_queue` list will receive JSON messages with the following structure:

```json
{
  "filePath": "inputs/largefile.dat",
  "startByte": 0,
  "endByte": 1048575
}
```

This message tells the worker to process the first 1MB of the file `inputs/largefile.dat`.

## Dependencies

-   Redisson: A Redis Java client with features of an in-memory data grid.
-   Jackson: Used by Redisson's `JsonJacksonCodec` for JSON serialization/deserialization.

## To-Do / Potential Improvements

-   **Externalize Configuration**: Move Redis settings, queue names, and chunk size to a properties file or environment variables.
-   **Implement a Logging Framework**: Replace `System.out.println` with a robust logging framework like SLF4J with Logback/Log4j2.
-   **Add Unit & Integration Tests**: To ensure reliability and prevent regressions.
-   **Error Handling**: Implement a more robust error handling strategy, such as a dead-letter queue for chunks that fail to be published.