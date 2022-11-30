package com.scanner.document.Saving;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.scanner.document.Model.Document;
import com.scanner.document.Model.GenericDocument;
import com.scanner.document.Utils.BackgroundWork;

import java.util.List;

public class GenericDocumentRepository {
    private static final String TAG = "DocumentRepoTag";
    private GenericDocumentDao genericDocumentDao;
    private LiveData<List<GenericDocument>> allGenericDocuments;

    public GenericDocumentRepository(Application application) {
        GenericDocumentDatabase database = GenericDocumentDatabase.getInstance(application);
        genericDocumentDao = database.documentDao();
        allGenericDocuments = genericDocumentDao.getAllDocuments();
    }

    public void insert(GenericDocument... genericDocuments){
        new BackgroundWork(null){
            @Override
            public void doInBackground() {
                super.doInBackground();
                genericDocumentDao.insert(genericDocuments[0]);
            }
        }.execute();
    }

    public void update(GenericDocument... genericDocuments){
        Log.d(TAG, "update: ");
        new BackgroundWork(null){
            @Override
            public void doInBackground() {
                super.doInBackground();
                Log.d(TAG, "doInBackground: Inside Update doIn");
                genericDocumentDao.update(genericDocuments[0]);
            }
        }.execute();
    }

    public void delete(GenericDocument... genericDocuments){
        Log.d(TAG, "delete: ");
        new BackgroundWork(null){
            @Override
            public void doInBackground() {
                super.doInBackground();
                Log.d(TAG, "doInBackground: Inside delete doIn");
                genericDocumentDao.delete(genericDocuments[0]);
            }
        }.execute();
    }

    public void deleteAllDocuments(GenericDocument... genericDocuments){
        new BackgroundWork(null){
            @Override
            public void doInBackground() {
                super.doInBackground();
                genericDocumentDao.deleteAllNotes();
            }
        }.execute();
    }

    public LiveData<List<GenericDocument>> getAllGenericDocuments(){
        return allGenericDocuments;
    }


}
