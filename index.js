import React, {Component} from 'react';
import {
    WebView,
    requireNativeComponent,
    NativeModules,
    Platform
} from 'react-native';

const {CustomWebViewManager} = NativeModules;

/**
 * React Native's WebView on Android does not allow picture uploads.
 * However, due to [this pull request](https://github.com/facebook/react-native/pull/15016) one can override parts of the built-in WebView to add hooks wherever necessary.
 *
 * This component will:
 *
 *   1. Use the built-in React Native WebView on iOS.
 *   2. Be a drop-in replacement for the Android WebView with the additional functionality for file uploads.
 *
 * This requires several Java files to work: CustomWebViewManager.java, CustomWebViewModule.java, and CustomWebViewPackage.java.
 * Additionally, the MainApplication.java file needs to be edited to include the new package.
 */

export default class CustomWebView extends Component {
    static propTypes = {
        ...WebView.propTypes
    };

    render() {
        const nativeConfig =
            Platform.OS === 'android'
                ? {
                    component: RCTCustomWebView,
                    viewManager: CustomWebViewManager
                }
                : null;
        return (
            <WebView
                ref={webview => (this.webview = webview)}
                {...this.props}
                nativeConfig={nativeConfig}
            />
        );
    }

    injectJavaScript(...args) {
        this.webview.injectJavaScript(...args);
    }

    reload(...args) {
        this.webview.reload(...args);
    }
}

const RCTCustomWebView = requireNativeComponent(
    'CustomWebView',
    CustomWebView,
    WebView.extraNativeComponentConfig
);