package com.wifidirect.milan.wifidirect.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wifidirect.milan.wifidirect.utils.FileUtils;
import com.wifidirect.milan.wifidirect.R;
import com.wifidirect.milan.wifidirect.activities.MainActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by milan on 1.12.15..
 */
public class FileTransfer extends Fragment {
    private static final int FILE_SELECT_CODE = 0;
    @Bind(R.id.textviewpath) TextView mTextViewPath;
    @Bind(R.id.relativelayoutchoosefile) RelativeLayout mRelativeLayoutChooseFile;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filetransfer, null);

        String name = getArguments().getString("name");

        // action bar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("File Transfer");

        // set subtitle on actionbar
        if (name != null) {
            ((MainActivity) getActivity()).mToolbar.setSubtitle(name);
        }

        // Butter knife
        ButterKnife.bind(this, view);

        // start file receiver
        if(MainActivity.mService != null) {
            MainActivity.mService.startFileReceiver();
        }

        askForStoragePermission();

        return view;
    }


    @OnClick(R.id.button_choosefile)
    public void chooseFile() {
        Intent intentChooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        intentChooseFile.setType("*/*");
        intentChooseFile.addCategory(Intent.CATEGORY_OPENABLE);

        // start activity for result
        startActivityForResult(Intent.createChooser(intentChooseFile, "Select a File to Send")
                , FILE_SELECT_CODE);
    }


    @OnClick(R.id.button_done)
    public void sendFile() {
        String path = mTextViewPath.getText().toString();
        if(path.equals("path") || path.equals("null")) {
            Toast.makeText(getActivity(), "First, choose file!", Toast.LENGTH_SHORT).show();
            return;
        }

        // send file
        if (MainActivity.mService != null) {
            MainActivity.mService.sendFile(path);
        }

        mRelativeLayoutChooseFile.setVisibility(View.GONE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case FILE_SELECT_CODE:
                // get the uri of the selected file
                Uri uri = data.getData();
                // get the path
                // String path = FileUtils.getRealPathFromURI(getActivity(), uri);
                String path = FileUtils.getPath(uri, getActivity());
                // set text view
                mTextViewPath.setText(String.valueOf(path));

                break;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /** Ask for user permission READ_EXTERNAL_STORAGE. */
    private void askForStoragePermission() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 2) {
            Toast.makeText(getActivity(), "thx!", Toast.LENGTH_SHORT).show();
        }
    }


    // Main Menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v
            , ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind butterknife
        ButterKnife.unbind(this);
        // stop file receiver
        if(MainActivity.mService != null) {
            MainActivity.mService.stopFileReceiver();
        }
    }


}
