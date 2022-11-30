package com.scanner.document.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(tableName = "ocr_document_table")
public class OcrDocument {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fileName;
    private ArrayList<String> filePhotos;
    private ArrayList<String> fileTexts;
    private long timeStamp;
    private String savedUri;

    public OcrDocument() {
        this.fileName = "Scan_" + System.currentTimeMillis();
        this.filePhotos = new ArrayList<>();
        this.fileTexts = new ArrayList<>();
        this.timeStamp = System.currentTimeMillis();
        this.savedUri = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<String> getFilePhotos() {
        return filePhotos;
    }

    public void setFilePhotos(ArrayList<String> filePhotos) {
        this.filePhotos = filePhotos;
    }

    public ArrayList<String> getFileTexts() {
        return fileTexts;
    }

    public void setFileTexts(ArrayList<String> fileTexts) {
        this.fileTexts = fileTexts;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSavedUri() {
        return savedUri;
    }

    public void setSavedUri(String savedUri) {
        this.savedUri = savedUri;
    }
}
