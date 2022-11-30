package com.scanner.document.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "document_table")
public class Document {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String fileName;
    private ArrayList<String> filePhotos;
    private long timeStamp;
    private String savedUri;

    public Document() {
        this.fileName = "Scan_" + System.currentTimeMillis();
        this.filePhotos = new ArrayList<>();
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

    public void insertImage(String imagePath){
        if(this.filePhotos==null){
            this.filePhotos = new ArrayList<>();
        }

        if(!this.filePhotos.contains(imagePath)){
            this.filePhotos.add(imagePath);
        }
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
