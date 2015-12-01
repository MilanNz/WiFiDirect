package com.wifidirect.milan.wifidirect.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by milan on 1.12.15..
 */
public class FileUtils {
    private static final String TAG = "FileUtils";


    /** Get path from contentresolver.
     * @param context
     * @param uri */
    public static String getPaths(Context context, Uri uri) {
        if("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try{
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }

            }catch (Exception e){
                Log.e(TAG, e.getMessage());

            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /** Get real path from uri.
     * @param context
     * @param contentUri */
    public static String getRealPathFromURI (Context context, Uri contentUri) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);

        if(cursor == null) {
            return contentUri.getPath();
        }

        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }



    private static String getRealPath(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }



    public static String getPath(Uri uri, Context context) {
        // will return image:x
        String wholeID = DocumentsContract.getDocumentId(uri);

        // split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);

        String filePath = "";

        int columntIndex = cursor.getColumnIndex(column[0]);

        if(cursor.getCount() == 0) {
            Log.e(TAG, "cursor : 0");
        }
        if(cursor.moveToFirst()) {
            filePath = cursor.getString(columntIndex);
        }

        cursor.close();
        return filePath;
    }



}
