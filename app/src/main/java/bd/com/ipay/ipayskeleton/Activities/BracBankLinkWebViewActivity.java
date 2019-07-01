package bd.com.ipay.ipayskeleton.Activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;

public class BracBankLinkWebViewActivity extends BaseActivity {

	public static final int CARD_TRANSACTION_FAILED = -1;
	public static final int CARD_TRANSACTION_CANCELED = 0;
	public static final int CARD_TRANSACTION_SUCCESSFUL = 1;

	public static final String URL_REGEX_APP_CARD_CANCELLED = "(.+)/Page/Cancel/";
	public static final String URL_REGEX_APP_CARD_FAILED = "(.+)/Page/Fail/";
	public static final String URL_REGEX_APP_TRANSACTION_CARD = "(.+)/Page/Success/";

	private static final String TRANSACTION_ID_POSITION = "$2";

	private MaterialDialog transactionCancelDialog;
	private WebView mWebView;
	private long bankId;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_or_debit_card_payment_web_view);
		mWebView = findViewById(R.id.web_view);
		final ProgressBar progressBar = findViewById(R.id.progress_bar);

		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progressBar.setProgress(newProgress);
			}
		});

		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

				if (error.getUrl().matches(Constants.VALID_IPAY_BD_ADDRESS)) {
					showSslErrorDialog(handler, error);
				} else {
					super.onReceivedSslError(view, handler, error);
				}
			}

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				return super.shouldInterceptRequest(view, request);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Logger.logD("Redirect URL", url);
				if (url.matches(URL_REGEX_APP_CARD_CANCELLED)) {
					finishWithResult(CARD_TRANSACTION_CANCELED);
					return true;
				} else if (url.matches(URL_REGEX_APP_CARD_FAILED)) {
					finishWithResult(CARD_TRANSACTION_FAILED);
					return true;
				} else if (url.matches(URL_REGEX_APP_TRANSACTION_CARD)) {
					finishWithResult(CARD_TRANSACTION_SUCCESSFUL);
					return true;
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}
		});
		mWebView.getSettings().setJavaScriptEnabled(true);
		final String cardPaymentUrl = getIntent().getStringExtra(Constants.CARD_PAYMENT_URL);
		bankId = getIntent().getLongExtra("BANK_ID",0);
		mWebView.loadUrl(cardPaymentUrl);
	}

	private void showSslErrorDialog(final SslErrorHandler handler, SslError error) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(BracBankLinkWebViewActivity.this);
		String message = "";
		switch (error.getPrimaryError()) {
			case SslError.SSL_UNTRUSTED:
				message = getString(R.string.ssl_untrustred_message);
				break;
			case SslError.SSL_EXPIRED:
				message = getString(R.string.ssl_expired_message);
				break;
			case SslError.SSL_IDMISMATCH:
				message = getString(R.string.ssl_id_mismatch_message);
				break;
			case SslError.SSL_NOTYETVALID:
				message = getString(R.string.ssl_not_yet_valid_message);
				break;
		}
		message += getString(R.string.do_you_want_to_continue);
		builder.setTitle(R.string.ssl_error_title);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.continue_message, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				handler.proceed();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				handler.cancel();
				finishWithResult(CARD_TRANSACTION_CANCELED);
			}
		});
		final AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			if (transactionCancelDialog != null && transactionCancelDialog.isShowing()) {
				return;
			}
			transactionCancelDialog = new MaterialDialog.Builder(this).
					content(R.string.card_transaction_cancel_warning).
					negativeText(R.string.no).
					positiveText(R.string.yes).
					onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							finishWithResult(CARD_TRANSACTION_CANCELED);
							dialog.cancel();
						}
					}).cancelable(false).build();
			transactionCancelDialog.show();
		}
	}

	private void finishWithResult(final int transactionStatusCode) {
		Intent intent = new Intent();
		intent.putExtra("BANK_ID", bankId);
		setResult(transactionStatusCode, intent);
		finish();
	}

	private void showTransactionErrorDialog(final Intent intent, String title, String message) {
		MaterialDialog transactionErrorDialog = new MaterialDialog.Builder(this).
				title(title).
				content(message).
				negativeText(R.string.ok).
				onNegative(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						dialog.cancel();
						setResult(RESULT_OK, intent);
						finish();
					}
				}).cancelable(false).build();
		transactionErrorDialog.show();
	}

	@Override
	protected Context setContext() {
		return this;
	}
}