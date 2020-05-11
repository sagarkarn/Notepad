package com.sagar.notepad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class EditorFragment extends Fragment implements TextWatcher {


    private TextView fileNameTv;
    private EditText textPage;
    private Spinner spinner;
    private AlertDialog dialog;
    private ProgressBar progressBar;

    private FilesViewModel viewModel;
    private String title;
    private String extension = ".txt";
    private String uri = "";
    public EditorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(FilesViewModel.class);

        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {

            title = getArguments().getString("title");
            assert title != null;
            setTitle(title);
            uri = getArguments().getString("uri");

        } else {
            title = "untitled*";
        }
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(title);


        textPage = view.findViewById(R.id.main_text_view);

        new TextPageLoader(textPage, uri).execute();

        textPage.requestFocus();

        @SuppressLint("InflateParams")
        View dialogView = getLayoutInflater().inflate(R.layout.save_dialog_layout, null);
        fileNameTv = dialogView.findViewById(R.id.dialog_file_name);
        spinner = dialogView.findViewById(R.id.dialog_spinner);
        progressBar = view.findViewById(R.id.progress_bar_edit);


        @SuppressLint("ResourceType")
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.extension, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        extension = ".txt";
                        break;
                    case 1:
                        extension = ".html";
                        break;
                    case 2:
                        extension = ".htm";
                        break;
                    case 3:
                        extension = ".json";
                        break;
                    case 4:
                        extension = ".js";
                        break;
                    case 5:
                        extension = ".css";
                        break;

                }
                Toast.makeText(getContext(),"SpinnerElement"+ i +"",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialog = new AlertDialog.Builder(requireContext())
                .setNegativeButton("cancel", null)
                .setPositiveButton("save", (dialogInterface, i) -> {
                    if (!checkIfExist()) {
                        saveToFile();
                    }
                    else {
                       AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                               .setMessage("File already exist \n\n Do you want to replace?")
                               .setPositiveButton("Yes",((dialogInterface1, i1) -> saveToFile()))
                               .setNegativeButton("No",((dialogInterface1, i1) -> dialog.show()))
                               .create();
                       alertDialog.show();
                    }
                })
                .setView(dialogView)
                .create();
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.SlidingDialog;

        requireActivity().getOnBackPressedDispatcher().addCallback(callback());


    }

    private boolean checkIfExist() {
        String fileName = Environment.getDataDirectory() + "/" + fileNameTv.getText().toString() + extension;
        File checkFile = new File(fileName);
        return checkFile.exists();
    }

    private String setTextInTextView(String uri) {
        if (uri != null) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                InputStream fileInputStream = requireActivity().getContentResolver().openInputStream(Uri.parse(uri));
                assert fileInputStream != null;
                BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }

        return "";
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                if (title.endsWith("*")) {
                    if (!title.equals("untitled*")) {
                        writeToFile(Uri.parse(uri), textPage.getText().toString(), title);
                    } else {
                        dialog.show();
                    }

                }
                break;
            case R.id.menu_cancel:
                requireActivity().onBackPressed();
        }
        return true;
    }


    private void saveToFile() {
        try {



            assert fileNameTv != null;
            String fileName = fileNameTv.getText().toString();
            if (!fileName.contains(".")) {
                fileName = fileName + extension;
            }
            boolean b = true, c = true;
            File file = new File(Environment.getExternalStorageDirectory()
                    + "/" + getResources().getString(R.string.app_name));
            if (!file.exists()) {
                b = file.mkdir();

            }
            file = new File(file.getPath() + "/" + fileName);
            if (!file.exists()) {
                c = file.createNewFile();
            }
            if (b && c) {
                uri = "content://com.android.externalstorage.documents/document/primary%3ANotepad%2F" + fileName;

                title = fileName;
                writeToFile(Uri.fromFile(file), textPage.getText().toString(), fileName);
            }

            dialog.dismiss();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (title.charAt(title.length() - 1) != '*') {
            title += "*";
        }
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(title);
    }

    private void setTitle(String title) {
        assert title != null;
        if (title.length() > 25) {
            String titleFirst = title.substring(0, 15);
            String titleLast = title.substring(title.length() - 6);
            title = titleFirst + "..." + titleLast;
        }
        this.title = title;
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(title);
    }

    private void writeToFile(Uri uriData, String data, String title) {

        FileOutputStream outputStream;
        try {
            String strUri;
            if (uriData.toString().contains("/primary") || uriData.toString().contains("content")) {
                strUri = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Objects.requireNonNull(uriData.getPath()).substring(uriData.getPath().indexOf(":") + 1).replace("external_files", "");
                Log.d("strUri", strUri);
            } else strUri = uriData.getPath();
            FileOutputStream pdf = null;
            if (strUri != null) {
                pdf = new FileOutputStream(new File(strUri));
            }

            outputStream = pdf;

            if (outputStream != null) {
                outputStream.write(data.getBytes());
            }
            setTitle(title);
            if (strUri != null) {
                viewModel.insert(new FilesModel(strUri, title));
            }

            if (outputStream != null) {
                outputStream.close();
            }
            if (pdf != null) {
                pdf.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Snackbar.make(requireView(), "FileNotFound", Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(requireView(), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        }
    }

    private OnBackPressedCallback callback() {
        return new OnBackPressedCallback(true) {
            AlertDialog dialog1 = new AlertDialog.Builder(requireContext())
                    .setMessage("Do you Want to save this file")
                    .setPositiveButton("save", (dialogInterface, i) -> {
                        if(title.equals("untitled*")) {
                            dialog.show();
                        }else saveToFile();
                    })
                    .setNegativeButton("Cancel", ((dialogInterface, i) -> {
                        remove();
                        requireActivity().onBackPressed();
                    }))
                    .create();

            @Override
            public void handleOnBackPressed() {
                if(title.endsWith("*")) {

                    dialog1.show();
                }
                else {
                    remove();
                    if(getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            }
        };
    }

    @SuppressLint("StaticFieldLeak")
    private class TextPageLoader extends AsyncTask<Void, Void, Void> {
        private EditText textView;
        private String uri;
        private String data;

        TextPageLoader(EditText textPage, String uri) {
            textView = textPage;
            this.uri = uri;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            data = setTextInTextView(uri);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            textView.setText(data);
            textView.setSelection(data.length());
            textPage.addTextChangedListener(EditorFragment.this);
        }
    }
}
