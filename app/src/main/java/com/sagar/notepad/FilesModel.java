package com.sagar.notepad;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FilesModel {

    @PrimaryKey
    @NonNull
    String uri;
    String name;


    FilesModel(@NonNull String uri,String name) {
        this.uri = uri;
        this.name = name;
    }

}
