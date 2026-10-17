package com.urooj.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;

import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    private static final String LOCAL_URL = "https://appassets.androidplatform.net/assets/index.html";
    private static final String ASSET_HOST = "appassets.androidplatform.net";
    private WebView webView;
    private LinearLayout errorPanel;
    private WebViewAssetLoader assetLoader;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(253, 251, 247));
        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        errorPanel = new LinearLayout(this);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setPadding(36, 28, 36, 28);
        errorPanel.setVisibility(View.GONE);
        TextView title = new TextView(this);
        title.setText("UROOJ could not open");
        title.setTextSize(21);
        title.setTextColor(Color.rgb(27, 77, 62));
        title.setGravity(Gravity.CENTER);
        TextView details = new TextView(this);
        details.setText("Please try loading the app again. Your saved progress is kept separately on this device.");
        details.setTextSize(14);
        details.setTextColor(Color.rgb(44, 53, 57));
        details.setGravity(Gravity.CENTER);
        details.setPadding(0, 14, 0, 18);
        Button retry = new Button(this);
        retry.setText("Try again");
        retry.setOnClickListener(v -> loadApp());
        errorPanel.addView(title);
        errorPanel.addView(details);
        errorPanel.addView(retry);
        root.addView(errorPanel, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) s.setSafeBrowsingEnabled(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(Uri.parse(url));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return route(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return route(Uri.parse(url));
            }
            private boolean route(Uri uri) {
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(java.util.Locale.ROOT);
                String host = uri.getHost() == null ? "" : uri.getHost();
                if ("https".equals(scheme) && ASSET_HOST.equalsIgnoreCase(host)) return false;
                // Only hand ordinary web links and email links to other apps.
                // Block javascript:, file:, content:, intent:, and arbitrary custom schemes.
                if (!"https".equals(scheme) && !"http".equals(scheme) && !"mailto".equals(scheme)) return true;
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
                } catch (Exception ignored) {}
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                errorPanel.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                if (request.isForMainFrame()) showError();
            }
        });
        if (savedInstanceState != null) webView.restoreState(savedInstanceState);
        else loadApp();
    }

    private void loadApp() {
        errorPanel.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(LOCAL_URL);
    }
    private void showError() {
        webView.setVisibility(View.GONE);
        errorPanel.setVisibility(View.VISIBLE);
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        webView.saveState(out);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
