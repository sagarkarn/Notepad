package com.sagar.notepad;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FilesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilesModel model);
    @Update
    void update(FilesModel model);
    @Delete
    void delete(FilesModel model);
    @Query("SELECT * FROM FilesModel")
    LiveData<List<FilesModel>> allFiles();
}
