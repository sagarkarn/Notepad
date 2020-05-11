package com.sagar.notepad;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = FilesModel.class, version = 2)
public abstract class FilesDatabase extends RoomDatabase {

    abstract FilesDao getFileDao();
    private static volatile FilesDatabase INSTANCE;
    final static ExecutorService databaseWriter = Executors.newFixedThreadPool(4);
    public static FilesDatabase getInstance(Context context){
        if(INSTANCE == null){
            synchronized (FilesDatabase.class){
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),FilesDatabase.class,"files-database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
