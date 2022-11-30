package com.scanner.document.Saving;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.scanner.document.Model.Document;
import com.scanner.document.Model.GenericDocument;

import java.util.List;

public class GenericDocumentModel extends AndroidViewModel {
    private GenericDocumentRepository repository;
    private LiveData<List<GenericDocument>> allGenericDocuments;

    public GenericDocumentModel(@NonNull Application application) {
        super(application);
        repository = new GenericDocumentRepository(application);
        allGenericDocuments = repository.getAllGenericDocuments();
    }

    public void insert(GenericDocument genericDocument){
        repository.insert(genericDocument);
    }

    public void update(GenericDocument genericDocument){
        repository.update(genericDocument);
    }

    public void delete(GenericDocument genericDocument){
        repository.delete(genericDocument);
    }

    public void deleteAllDocuments(GenericDocument genericDocument){
        repository.deleteAllDocuments(genericDocument);
    }

    public LiveData<List<GenericDocument>> getAllGenericDocuments(){
        return allGenericDocuments;
    }
}
