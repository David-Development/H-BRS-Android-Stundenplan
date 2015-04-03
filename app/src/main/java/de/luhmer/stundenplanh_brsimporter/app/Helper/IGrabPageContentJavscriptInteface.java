package de.luhmer.stundenplanh_brsimporter.app.Helper;

import android.webkit.JavascriptInterface;

/**
 * Created by David on 06.04.2014.
 */
public interface IGrabPageContentJavscriptInteface {
    @JavascriptInterface
    public void processHTML(String html);
}
