package com.invobox.reactnative.webview;

import android.net.Uri;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.facebook.react.common.build.ReactBuildConfig;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.views.webview.ReactWebViewManager;

@ReactModule(name = CustomWebViewManager.REACT_CLASS)
public class CustomWebViewManager extends ReactWebViewManager {
    protected static final String REACT_CLASS = "CustomWebView";

    private CustomWebViewPackage aPackage;

    // Pulled this code from React Native itself;
    // https://github.com/facebook/react-native/blob/59d9f8ca5eb96b4b455b60ed170dfb05bc9c7251/ReactAndroid/src/main/java/com/facebook/react/views/webview/ReactWebViewManager.java#L361
    @Override
    protected WebView createViewInstance(final ThemedReactContext reactContext) {
        ReactWebView webView = (ReactWebView) super.createViewInstance(reactContext);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                if (ReactBuildConfig.DEBUG) {
                    return super.onConsoleMessage(message);
                }

                // Ignore console logs in non debug builds.
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                return getModule().startPhotoPickerIntent(filePathCallback, fileChooserParams);
            }
        });

        WebView.setWebContentsDebuggingEnabled(true);
        return webView;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected void addEventEmitters(ThemedReactContext reactContext, WebView view) {
        view.setWebViewClient(new CustomWebViewClient());
    }

    public CustomWebViewPackage getPackage() {
        return this.aPackage;
    }

    public void setPackage(CustomWebViewPackage aPackage) {
        this.aPackage = aPackage;
    }

    public CustomWebViewModule getModule() {
        return this.aPackage.getModule();
    }

    protected static class CustomWebViewClient extends ReactWebViewClient {
    }
}