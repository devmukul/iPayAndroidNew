package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.annotation.SuppressLint;
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

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;

public class SaveCardWebViewActivity extends BaseActivity {

	public static final int CARD_TRANSACTION_FAILED = -1;
	public static final int CARD_TRANSACTION_CANCELED = 0;
	public static final int CARD_TRANSACTION_SUCCESSFUL = 1;

	public static final String URL_REGEX_APP_CARD_CANCELLED = "(.+)save-status=cancelled";
	public static final String URL_REGEX_APP_CARD_FAILED = "(.+)save-status=failed";
	public static final String URL_REGEX_APP_TRANSACTION_CARD = "(.+)save-status=success";

	private static final String TRANSACTION_ID_POSITION = "$2";

	private MaterialDialog transactionCancelDialog;
	private WebView mWebView;

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
					finishWithResult(CARD_TRANSACTION_CANCELED, null);
					return true;
				} else if (url.matches(URL_REGEX_APP_CARD_FAILED)) {
					finishWithResult(CARD_TRANSACTION_FAILED, null);
					return true;
				} else if (url.matches(URL_REGEX_APP_TRANSACTION_CARD)) {
					finishWithResult(CARD_TRANSACTION_SUCCESSFUL, null);
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
		mWebView.loadUrl(cardPaymentUrl);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDisplayZoomControls(false);
	}

	private void showSslErrorDialog(final SslErrorHandler handler, SslError error) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(SaveCardWebViewActivity.this);
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
				finishWithResult(CARD_TRANSACTION_CANCELED, null);
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
							finishWithResult(CARD_TRANSACTION_CANCELED, null);
							dialog.cancel();
						}
					}).cancelable(false).build();
			transactionCancelDialog.show();
		}
	}

	private void finishWithResult(final int transactionStatusCode, final String transactionId) {
		Intent intent = new Intent();
		intent.putExtra(Constants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD_STATUS, transactionStatusCode);

		switch (transactionStatusCode) {
			case CARD_TRANSACTION_CANCELED:
				showTransactionErrorDialog(intent, getString(R.string.card_save_cancel_dialog_text), getString(R.string.card_save_cancel_dialog_msg));
				break;
			case CARD_TRANSACTION_FAILED:
				showTransactionErrorDialog(intent, getString(R.string.card_save_fail_dialog_text), getString(R.string.card_save_fail_dialog_msg));
				break;
			case CARD_TRANSACTION_SUCCESSFUL:
				ProfileInfoCacheManager.addSourceOfFund(true);
				intent.putExtra(Constants.TRANSACTION_ID, transactionId);
				setResult(RESULT_OK, intent);
				finish();
				break;
		}
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