package com.sagar.notepad;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

public class FileRepo {
    private LiveData<List<FilesModel>> allFiles;
    private FilesDao dao;
    FileRepo(Context context){
        FilesDatabase database = FilesDatabase.getInstance(context);
        dao = database.getFileDao();
        allFiles = dao.allFiles();
    }
    void insert(FilesModel  model){
        FilesDatabase.databaseWriter.execute(() -> dao.insert(model));
    }
    void update(FilesModel  model){
        FilesDatabase.databaseWriter.execute(()-> dao.update(model));
    }
    void delete(FilesModel  model){
        FilesDatabase.databaseWriter.execute(() -> dao.delete(model));
    }
    public LiveData<List<FilesModel>> getAllFiles(){
        return allFiles;
    }
}
