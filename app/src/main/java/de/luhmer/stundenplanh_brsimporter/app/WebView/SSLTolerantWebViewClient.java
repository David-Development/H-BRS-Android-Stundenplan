package de.luhmer.stundenplanh_brsimporter.app.WebView;

import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by David on 31.03.2014.
 */
// SSL Error Tolerant Web View Client
public class SSLTolerantWebViewClient extends WebViewClient {

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed(); // Ignore SSL certificate errors
    }

}
