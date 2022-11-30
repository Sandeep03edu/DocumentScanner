package com.scanner.document.Saving;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.scanner.document.Model.Document;
import com.scanner.document.Model.GenericDocument;
import com.scanner.document.Scanning.Converter;

@Database(entities = {GenericDocument.class}, version = 1)
@TypeConverters(Converter.class)
public abstract class GenericDocumentDatabase extends RoomDatabase {

    public static GenericDocumentDatabase instance;
    public abstract GenericDocumentDao documentDao();

    public static synchronized GenericDocumentDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            GenericDocumentDatabase.class, "generic_document_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }
    private static Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).BackgroundTask();
        }
    };



    private static class PopulateDbAsyncTask{

        private GenericDocumentDao documentDao;

        public PopulateDbAsyncTask(GenericDocumentDatabase db) {
            documentDao = db.documentDao();
        }

        public void BackgroundTask() {
            new NoteDatabaseAsyncTask(){
                @Override
                public void doInBackground() {
                    super.doInBackground();
                }
            }.execute();
        }

    }
    public abstract static class NoteDatabaseAsyncTask {

        public NoteDatabaseAsyncTask() {  }
        public void startBackground() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doInBackground();
                    // We don't need to perform postExecute task
//                    activity.runOnUiThread(() -> onPostExecute());
                }
            }).start();
        }

        public void execute() {
            startBackground();
        }

        public void doInBackground() {}

        public void onPostExecute() {}

    }

}
