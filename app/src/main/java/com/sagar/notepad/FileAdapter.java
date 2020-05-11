package com.sagar.notepad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.customview.widget.Openable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private List<FilesModel> models = new ArrayList<>();
    private Activity mActivity;
    private Context context;
    private View mView;

    private ActionMode.Callback callback = null;
    private ActionMode mActionMode;

    private List<FilesModel> selectedModel = new ArrayList<>();
    private FilesViewModel viewModel;


    FileAdapter(Activity activity,View view){
        mActivity = activity;
        mView=view;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_file_name,parent,false);
        return new FileViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {

        viewModel = new ViewModelProvider((ViewModelStoreOwner)mActivity).get(FilesViewModel.class);
        FilesModel model = models.get(position);
        holder.layout.setBackgroundResource(R.drawable.recycle_item_bg);
        final Cursor returnCursor = mActivity.getContentResolver().query(Uri.parse(model.uri),null,null,null,null);
        String fileName = "";
        long fileSize = 0;
        long lastModify = 0L;
        if(returnCursor != null && returnCursor.moveToFirst()) {

            final int nameIndex = Objects.requireNonNull(returnCursor).getColumnIndex(OpenableColumns.DISPLAY_NAME);
            final int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            final int lastModifyIndex = returnCursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            Log.d("FilePath",getRealPathFromURI(Uri.parse(model.uri)));

            returnCursor.moveToFirst();

            fileName = returnCursor.getString(nameIndex);
            fileSize = returnCursor.getLong(sizeIndex);
            lastModify = returnCursor.getLong(lastModifyIndex);



        }
        else {
            if(model.uri.contains("/storage")){
                File file = new File(model.uri);

                fileName = file.getName();
                fileSize = file.length();



            }
        }
        Log.d("FileUri",model.uri);
        if(fileSize > 1024) {
            holder.fileSize.setText((float)(fileSize/1024)+"kb");
        }
        else {holder.fileSize.setText(fileSize + "b");}
        holder.fileName.setText(fileName);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date = dateFormat.format(new Date(lastModify));

        holder.fileModifyDate.setText(date);

        String finalFileName = fileName;


        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isSelected(model)) {
                    selectedModel.add(model);
                    holder.layout.setBackgroundColor(Color.CYAN);
                }
                else{
                    selectedModel.remove(model);
                    holder.layout.setBackgroundResource(R.drawable.recycle_item_bg);
                }
                if (callback == null) {
                    FileAdapter.this.createObject();
                }
                mActivity.startActionMode(callback);
                return true;

            }
        });

        holder.layout.setOnClickListener(view -> {
            if(selectedModel.size() == 0) {
                if(callback != null){
                    mActionMode.finish();
                    mActionMode = null;
                    callback = null;
                }
                Bundle bundle = new Bundle();
                bundle.putString("title", finalFileName);
                bundle.putString("uri", model.uri);
                Navigation.findNavController(mView).navigate(R.id.action_fileListFragment_to_editorFragment, bundle);
            }
            else {
                if(!isSelected(model)) {
                    selectedModel.add(model);
                    holder.layout.setBackgroundColor(Color.CYAN);
                }
                else{
                    selectedModel.remove(model);
                    holder.layout.setBackgroundResource(R.drawable.recycle_item_bg);
                    if(selectedModel.size() == 0){
                        mActionMode.finish();
                        mActionMode = null;
                        callback = null;
                    }
                }
            }
        });

    }

    private boolean isSelected(FilesModel model) {
        return selectedModel.contains(model);
    }

    private void createObject() {

        callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.action_mode_menu,menu);
                mActionMode = actionMode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.action_mode_delete){
                    CheckBox checkBox = new CheckBox(mActivity);

                    List<FilesModel> list = new ArrayList<>(selectedModel);
                    selectedModel.clear();

                    for(int i = 0; i < list.size(); i++){
                        viewModel.delete(list.get(i));
//                        if(checkBox.isChecked()){

                            final File file = new File(list.get(i).uri);
                            if(file.delete()){

                                Snackbar.make(mView,"File Deleted",Snackbar.LENGTH_SHORT).show();

                            }
                            else Snackbar.make(mView,"File Deleted",Snackbar.LENGTH_SHORT).show();
//                        }
                    }
                    if(models.size() == 1) {
                        models.clear();
                    }
                    list.clear();
                    notifyDataSetChanged();
                    if(actionMode != null) {
                        actionMode.finish();
                        callback = null;
                    }

                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                notifyDataSetChanged();
                selectedModel.clear();
                callback = null;
            }
        };

    }

    @Override
    public int getItemCount() {
        return models.size();
    }
    void setAdapter(List<FilesModel> models){
        this.models = models;
        notifyDataSetChanged();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName,fileSize,fileModifyDate;
        ConstraintLayout layout;
        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.single_file_name);
            fileSize = itemView.findViewById(R.id.single_file_size);
            fileModifyDate = itemView.findViewById(R.id.single_last_modify);
            layout = itemView.findViewById(R.id.rootView);

        }
    }
    public String getRealPathFromURI(Uri contentUri)
    {
        return contentUri.getPath();
    }

}