package com.invobox.ReactNative.WebView;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.io.File;

public class CustomWebViewModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public static final String REACT_CLASS = "CustomWebViewModule";

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private final static int SELECT_FILE_LEGACY = 3;

    private CustomWebViewPackage aPackage;

    private ValueCallback<Uri> filePathCallback;
    private ValueCallback<Uri[]> filePathCallbackArr;
    private WebChromeClient.FileChooserParams fileChooserParams;
    private Uri outputFileUri;

    public CustomWebViewModule(ReactApplicationContext context) {
        super(context);
        context.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (isNewApi() && this.filePathCallbackArr == null) {
            return;
        } else if(this.filePathCallbackArr == null){
            return;
        }

        switch (requestCode) {
            case REQUEST_CAMERA:
//                Uri cameraResult = ((data == null || resultCode != Activity.RESULT_OK) ? null : data.getData());
                if(resultCode == Activity.RESULT_OK)
                    filePathCallback.onReceiveValue(outputFileUri);
                break;
            case SELECT_FILE_LEGACY:
                Uri result = ((data == null || resultCode != Activity.RESULT_OK) ? null : data.getData());
                this.filePathCallback.onReceiveValue(result);
            case SELECT_FILE:
                if(this.isNewApi()){
                    this.filePathCallbackArr.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    this.filePathCallbackArr = null;
                }

                break;
        }
        this.filePathCallback = null;
        this.filePathCallbackArr = null;
    }


    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        this.onActivityResult(requestCode, resultCode, data);
    }

    public void onNewIntent(Intent intent) {}

    // For Android 4.1+
    public boolean startFileChooserIntent(ValueCallback<Uri> uploadMessage, String acceptType) {
        this.filePathCallback = uploadMessage;
        this.filePickerCustomIntent();

        return true;
    }

    // For Android 5.0+
    public boolean startFileChooserIntent(ValueCallback<Uri[]> filePathCallback, final WebChromeClient.FileChooserParams fileChooserParams) {
        this.filePathCallbackArr = filePathCallback;
        this.fileChooserParams = fileChooserParams;
        this.filePickerCustomIntent();

        return true;
    }

    private void filePickerCustomIntent(){

        final String TAKE_PHOTO = "Use camera";
        final String CHOOSE_FILE = "From the gallery";
        final String CANCEL = "Cancel";
        final String DIALOG_TITLE = "Add a file";
        final String ACCEPT_TYPE = "*/*";

        final CharSequence[] items = {TAKE_PHOTO, CHOOSE_FILE, CANCEL};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getCurrentActivity());
        builder.setTitle(DIALOG_TITLE);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(TAKE_PHOTO)) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    try {
                        outputFileUri = Uri.fromFile(File.createTempFile("file-", ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
                    } catch (java.io.IOException e) {
                    }
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

//                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "temp.jpg");
//                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    getCurrentActivity().startActivityForResult(cameraIntent, REQUEST_CAMERA);
                }
                else if(items[item].equals(CHOOSE_FILE)){
                    Activity currentActivity = getCurrentActivity();
                    if (currentActivity == null) {
                        if(isNewApi()) {
                            filePathCallbackArr.onReceiveValue(null);
                        } else {
                            filePathCallback.onReceiveValue(null);
                        }
                    }

                    if(isNewApi()){
                        try {
                            currentActivity.startActivityForResult(fileChooserParams.createIntent().setType("*/*"), SELECT_FILE, new Bundle());
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();

                            if (filePathCallbackArr != null) {
                                filePathCallbackArr.onReceiveValue(null);
                                filePathCallbackArr = null;
                            }
                        }
                    } else {
                        Intent intentChoose = new Intent(Intent.ACTION_GET_CONTENT);
                        intentChoose.addCategory(Intent.CATEGORY_OPENABLE);
                        intentChoose.setType(ACCEPT_TYPE);

                        try {
                            currentActivity.startActivityForResult(intentChoose, SELECT_FILE_LEGACY, new Bundle());
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();

                            if (filePathCallback != null) {
                                filePathCallback.onReceiveValue(null);
                                filePathCallback = null;
                            }
                        }
                    }
                }
                else if(items[item].equals(CANCEL)){
                    dialog.dismiss();
                    if(isNewApi()) {
                        filePathCallbackArr.onReceiveValue(null);
                    } else {
                        filePathCallback.onReceiveValue(null);
                    }
                }
            }
        });
        builder.show();
    }

    public CustomWebViewPackage getPackage() {
        return this.aPackage;
    }

    public void setPackage(CustomWebViewPackage aPackage) {
        this.aPackage = aPackage;
    }

    protected boolean isNewApi(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}