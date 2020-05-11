package com.sagar.notepad;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FileListFragment extends Fragment {

    private static final int FILE_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private FloatingActionButton floatingActionButton,fabOpen,fabNew;
    private TextView textOpen, textNew;
    private NavController controller;
    private Activity mActivity;
    private RecyclerView recyclerView;
    FileAdapter adapter;

    private Animation fab_open_rotate,fab_close_rotate,fab_open_anim,fab_close_anim;
    private FilesViewModel filesViewModel;

    public FileListFragment() {
        // Required empty public constructor
    }
    private boolean isOpen = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isOpen = false;

        mActivity = requireActivity();


        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull final View layoutView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(layoutView, savedInstanceState);

        filesViewModel = new ViewModelProvider(this).get(FilesViewModel.class);

        recyclerView = layoutView.findViewById(R.id.file_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new FileAdapter(requireActivity(),layoutView);
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            }
        }
        else {
            recyclerView.setAdapter(adapter);
            LiveData<List<FilesModel>> liveData = filesViewModel.getAllFiles();

            liveData.observe(getViewLifecycleOwner(), models -> {
                List<FilesModel> modelList = new ArrayList<>();
                for(int i = models.size() - 1; i >= 0;i--){
                    modelList.add(models.get(i));
                    adapter.setAdapter(modelList);
                }

            });
        }



        recyclerView.setHasFixedSize(true);
        recyclerView.setAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.recycler_view_anim));






        controller = Navigation.findNavController(layoutView);


        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.app_name);

        floatingActionButton = layoutView.findViewById(R.id.floatingActionButton);
        fabOpen = layoutView.findViewById(R.id.fab_open);
        fabNew = layoutView.findViewById(R.id.fab_new);

        textOpen = layoutView.findViewById(R.id.fab_text_open_file);
        textNew = layoutView.findViewById(R.id.fab_text_new_file);

        fab_open_rotate = AnimationUtils.loadAnimation(getContext(),R.anim.fab_open_rotate);
        fab_close_rotate = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close_rotate);
        fab_open_anim = AnimationUtils.loadAnimation(getContext(),R.anim.fab_open_item);
        fab_close_anim = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close_item);



        floatingActionButton.setOnClickListener((view)->{
                if(!isOpen) {
                    floatingActionButton.startAnimation(fab_open_rotate);
                    fabNew.startAnimation(fab_open_anim);
                    fabOpen.startAnimation(fab_open_anim);
                    textNew.startAnimation(fab_open_anim);
                    textOpen.startAnimation(fab_open_anim);

                    fabNew.setVisibility(View.VISIBLE);
                    fabOpen.setVisibility(View.VISIBLE);
                    textNew.setVisibility(View.VISIBLE);
                    textOpen.setVisibility(View.VISIBLE);

                    floatingActionButton.setRotation(45f);
                    isOpen = true;

                }
                else{

                    floatingActionButton.startAnimation(fab_close_rotate);
                    fabNew.startAnimation(fab_close_anim);
                    fabOpen.startAnimation(fab_close_anim);
                    textNew.startAnimation(fab_close_anim);
                    textOpen.startAnimation(fab_close_anim);

                    fabNew.setVisibility(View.GONE);
                    fabOpen.setVisibility(View.GONE);
                    textNew.setVisibility(View.GONE);
                    textOpen.setVisibility(View.GONE);

                    floatingActionButton.setRotation(0);

                    isOpen = false;

                }


        });

        fabNew.setOnClickListener(view -> controller.navigate(R.id.action_fileListFragment_to_editorFragment));
        fabOpen.setOnClickListener(view-> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent,FILE_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    filesViewModel.insert(new FilesModel(Objects.requireNonNull(data.getData()).toString(),"notepad"));
                    getData(data.getData());
                }
            }
        }
    }

    private void getData(Uri dataUri) {
        Log.d("datauri",dataUri.toString());

        @SuppressLint("Recycle")
        Cursor returnCursor = mActivity.getContentResolver().query(dataUri,null,null,null,null);

        int nameIndex = Objects.requireNonNull(returnCursor).getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        String fileName = returnCursor.getString(nameIndex);
        long fileSize = returnCursor.getLong(sizeIndex);

        Bundle bundle = new Bundle();
        bundle.putString("title",fileName);
        bundle.putString("uri",dataUri.toString());
        Log.d("title",fileName);
        Log.d("uri",dataUri.toString());
        controller.navigate(R.id.action_fileListFragment_to_editorFragment,bundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recyclerView.setAdapter(adapter);
                LiveData<List<FilesModel>> liveData = filesViewModel.getAllFiles();

                liveData.observe(getViewLifecycleOwner(), models -> {
                    List<FilesModel> modelList = new ArrayList<>();
                    for (int i = models.size() - 1; i >= 0; i--) {
                        modelList.add(models.get(i));
                        adapter.setAdapter(modelList);
                    }

                });
            } else {
                floatingActionButton.setEnabled(false);
                Snackbar.make(requireView(), "All Functions are disabled", Snackbar.LENGTH_INDEFINITE).show();
            }
        }

    }
}