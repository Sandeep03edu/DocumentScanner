package com.scanner.document.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "generic_document_table")
public class GenericDocument {
    @PrimaryKey(autoGenerate = true)
    private int id;
    Document document;
    OcrDocument ocrDocument;
    Long timeStamp;

    public GenericDocument() {
        timeStamp = System.currentTimeMillis();
    }


    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public OcrDocument getOcrDocument() {
        return ocrDocument;
    }

    public void setOcrDocument(OcrDocument ocrDocument) {
        this.ocrDocument = ocrDocument;
    }
}
