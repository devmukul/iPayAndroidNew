package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import bd.com.ipay.ipayskeleton.R;


public class WebViewActivity extends BaseActivity {
    private WebView webView;
    private String uriString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        final Uri uri = Uri.parse(getIntent().getStringExtra("url"));
        uriString = uri.toString();
        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.loadUrl(uri.toString());
    }

    @Override
    protected Context setContext() {
        return WebViewActivity.this;
    }

    @Override
    public void onBackPressed() {
        if (getIntent() != null) {
            if (getIntent().hasExtra("sourceActivity")) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    super.onBackPressed();
                }
            } else {
                if (uriString != null && !uriString.isEmpty()) {
                    if (uriString.toLowerCase().contains("promotion")) {
                        Intent intent = new Intent(this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            super.onBackPressed();
                        }
                    }
                } else {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        super.onBackPressed();
                    }
                }

            }
        } else {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        }
        finish();
    }
}