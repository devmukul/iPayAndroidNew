package bd.com.ipay.ipayskeleton.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class DocumentPreviewActivity extends BaseActivity {

	// Load a url (of an image, a pdf etc) in a webview.
	// File extension can be .pdf, .jpg etc. If you want to load a web page instead
	// of a file, pass null or empty string here.

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String documentUrl = getIntent().getStringExtra(Constants.DOCUMENT_URL);
		String fileExtension = getIntent().getStringExtra(Constants.FILE_EXTENSION);
		String documentTypeName = getIntent().getStringExtra(Constants.DOCUMENT_TYPE_NAME);

		if (documentTypeName != null)
			setTitle(documentTypeName);
		else
			setTitle("Attachment");

		WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setPluginState(WebSettings.PluginState.ON);
		webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDisplayZoomControls(false);

		if (documentUrl.contains("pdf")) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(documentUrl));
			startActivity(browserIntent);
		} else if (fileExtension.endsWith("jpg") || fileExtension.endsWith("jpeg") || fileExtension.endsWith("png")) {
			// For loading images
			webView.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%25;} </style></head><body><img src='" +
					documentUrl + "'/></body></html>", "text/html", "UTF-8");
		} else {
			webView.loadUrl(documentUrl);
		}

		setContentView(webView);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Context setContext() {
		return DocumentPreviewActivity.this;
	}
}