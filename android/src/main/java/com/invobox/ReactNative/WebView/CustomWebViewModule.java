package com.invobox.ReactNative.WebView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
    private CustomWebViewPackage aPackage;
    private ValueCallback<Uri[]> filePathCallback;
    private Uri outputFileUri;

    public CustomWebViewModule(ReactApplicationContext context) {
        super(context);
        context.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (this.filePathCallback == null) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CAMERA:
                if(resultCode == Activity.RESULT_OK)
                    filePathCallback.onReceiveValue(new Uri[]{outputFileUri});
                break;
            case SELECT_FILE:
                filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                break;
        }

        this.filePathCallback = null;
    }

    public void onNewIntent(Intent intent) {
    }

    public boolean startPhotoPickerIntent(final ValueCallback<Uri[]> filePathCallback, final WebChromeClient.FileChooserParams fileChooserParams) {
        final String TAKE_PHOTO = "Use camera";
        final String CHOOSE_FILE = "From the gallery";
        final String CANCEL = "Cancel";
        final String DIALOG_TITLE = "Add a file";
        final String FILE_TYPE = "*/*";
        final CharSequence[] items = {TAKE_PHOTO, CHOOSE_FILE, CANCEL};

        this.filePathCallback = filePathCallback;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getCurrentActivity());
        builder.setTitle(DIALOG_TITLE);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(TAKE_PHOTO)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        outputFileUri = Uri.fromFile(
                            File.createTempFile(
                                "file-", ".jpg",
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            )
                        );
                    } catch (java.io.IOException e) {
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    getCurrentActivity().startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals(CHOOSE_FILE)) {
                    getCurrentActivity().startActivityForResult(
                            fileChooserParams.createIntent().setType(FILE_TYPE),
                            SELECT_FILE
                    );
                } else if (items[item].equals(CANCEL)) {
                    dialog.dismiss();
                    filePathCallback.onReceiveValue(null);
                }
            }
        });
        builder.show();

        return true;
    }

    public CustomWebViewPackage getPackage() {
        return this.aPackage;
    }

    public void setPackage(CustomWebViewPackage aPackage) {
        this.aPackage = aPackage;
    }
}