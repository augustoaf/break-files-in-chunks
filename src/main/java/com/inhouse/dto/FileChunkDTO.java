package com.inhouse.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileChunkDTO {

    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("start_byte")
    private long startByte;
    @JsonProperty("end_byte")
    private long endByte;

    public FileChunkDTO(String filePath, long startByte, long endByte) {
        this.filePath = filePath;
        this.startByte = startByte;
        this.endByte = endByte;
    }
    public String getFilePath() {
        return filePath;
    }       

    public long getStartByte() {
        return startByte;
    }

    public long getEndByte() {
        return endByte;
    }
}
