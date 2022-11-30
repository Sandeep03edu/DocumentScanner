package com.scanner.document.Saving;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.scanner.document.Model.Document;
import com.scanner.document.Model.GenericDocument;

import java.util.List;

@Dao
public interface GenericDocumentDao {

    @Insert
    void insert(GenericDocument document);

    @Update
    void update(GenericDocument document);

    @Delete
    void delete(GenericDocument document);

    @Query("DELETE FROM generic_document_table")
    void deleteAllNotes();

    @Query("SELECT * FROM generic_document_table ORDER BY timeStamp DESC")
    LiveData<List<GenericDocument>> getAllDocuments();
}
