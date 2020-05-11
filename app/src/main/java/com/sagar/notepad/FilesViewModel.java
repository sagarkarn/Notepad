package com.sagar.notepad;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class FilesViewModel extends AndroidViewModel {
    private FileRepo repo;
    private LiveData<List<FilesModel>> allFiles;
    public FilesViewModel(@NonNull Application application) {
        super(application);
        repo = new FileRepo(application);
        allFiles = repo.getAllFiles();
    }

    public void insert(FilesModel model){
        repo.insert(model);
    }
    public void update(FilesModel model){
        repo.update(model);
    }
    public void delete(FilesModel model){
        repo.delete(model);
    }
    public LiveData<List<FilesModel>> getAllFiles(){
        return allFiles;
    }
}
