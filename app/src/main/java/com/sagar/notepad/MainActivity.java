package com.sagar.notepad;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.RotateAnimation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent dataIntent = getIntent();
        if(dataIntent != null) {
            String type = dataIntent.getType();
            String action = dataIntent.getAction();

            assert type != null;
            if ((Intent.ACTION_SEND.equals(action)) && type.contains("text")){

                Uri uri = dataIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                assert uri != null;
                getData(uri);
            }
            else if(Intent.ACTION_VIEW.equals(action) && type.contains("text")){
                Uri uri = dataIntent.getData();


                assert uri != null;

                getData(uri);
            }
        }



    }
    private void getData(Uri dataUri) {
        Log.d("datauri",dataUri.toString());

        @SuppressLint("Recycle")
        Cursor returnCursor = getContentResolver().query(dataUri,null,null,null,null);

        int nameIndex = Objects.requireNonNull(returnCursor).getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        String fileName = returnCursor.getString(nameIndex);
        long fileSize = returnCursor.getLong(sizeIndex);


        Bundle bundle = new Bundle();
        bundle.putString("title",fileName);
        bundle.putString("uri",dataUri.toString());

        Navigation.findNavController(this,R.id.nav_host_fragment).navigate(R.id.action_fileListFragment_to_editorFragment,bundle);
    }
}
