package bd.com.ipay.ipayskeleton.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.QRCodePaymentActivity;
import bd.com.ipay.ipayskeleton.R;

public class DialogUtils {
	public static MaterialDialog showAppUpdateDialog;

	public static void showAppUpdateRequiredDialog(final Context mContext) {
		if (showAppUpdateDialog != null) {
			return;
		}
		showAppUpdateDialog = new MaterialDialog.Builder(mContext)
				.title(R.string.update_ipay)
				.cancelable(false)
				.content(R.string.update_your_application)
				.positiveText(R.string.update_now)
				.negativeText(R.string.exit)
				.show();

		showAppUpdateDialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
			@Override
			public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
				Utilities.goToiPayInAppStore(mContext);
				((Activity) mContext).finish();
			}
		});

		showAppUpdateDialog.getBuilder().onNegative(new MaterialDialog.SingleButtonCallback() {
			@Override
			public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
				dialog.dismiss();
				((Activity) mContext).finish();
			}
		});

		showAppUpdateDialog.show();
	}

	public static void showChangePasswordSuccessDialog(final Context context) {
		MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
		dialog
				.title(R.string.change_password_success)
				.content(R.string.change_password_success_message)
				.cancelable(false)
				.positiveText(R.string.ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						((MyApplication) (((SecuritySettingsActivity) context).getApplication())).launchLoginPage(null);
					}
				})
				.show();
	}

	public static void showServiceNotAllowedDialog(final Context context) {
		showAlertDialog(context, context.getString(R.string.contact_support_message));
	}

	public static void showAlertDialog(final Context context, String message) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(false)
				.content(message)
				.negativeText(R.string.cancel)

				.show();
		dialog.show();
	}

	public static void showCancelableAlertDialog(final Context context, String message) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(true)
				.content(message)
				.negativeText(R.string.ok)
				.show();
		dialog.show();
	}

	public static void showDialogForInvalidQRCode(final Activity activity, String message) {
		MaterialDialog materialDialog;
		MaterialDialog.Builder materialDialogBuilder = new MaterialDialog.Builder(activity);
		materialDialogBuilder.positiveText(R.string.ok);
		materialDialogBuilder.content(message);
		materialDialogBuilder.dismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				Intent intent;
				intent = new Intent(activity, QRCodePaymentActivity.class);
				activity.startActivity(intent);
				activity.finish();

			}
		});
		materialDialog = materialDialogBuilder.build();
		materialDialog.show();
	}

	public static void showDialogForBusinessRuleNotAvailable(final Context context) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(false)
				.content(context.getString(R.string.service_unavailable_support_message))
				.positiveText(R.string.ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						((Activity) context).onBackPressed();
					}
				})
				.show();
		dialog.show();
	}

	public static void showDialogForNotEntitledForInstantMoney(final Context context) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(false)
				.content(context.getString(R.string.instant_money_not_entitled))
				.positiveText(R.string.ok)
				.show();
		dialog.show();
	}

	public static void showDialogVerificationRequired(final Context context) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(false)
				.content(context.getString(R.string.verification_required_message))
				.positiveText(R.string.ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						((Activity) context).onBackPressed();
					}
				})
				.show();
		dialog.show();
	}

	public static void showProfilePictureUpdateRestrictionDialog(Context context) {
		MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
		dialog
				.content(R.string.can_not_change_picture)
				.cancelable(false)
				.positiveText(R.string.got_it)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						dialog.dismiss();
					}
				})
				.show();
	}

	public static void showDialogOwnNumberErrorDialog(final Context context, final View view) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(false)
				.content(context.getString(R.string.you_cannot_send_money_to_your_number))
				.positiveText(R.string.ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						view.callOnClick();
					}
				})
				.show();
		dialog.show();
	}

	public static void showBalanceErrorInTransaction(final Context context, String errorMessage) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(false)
				.content(errorMessage)
				.positiveText(R.string.ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						((Activity) context).onBackPressed();
					}
				})
				.show();
		dialog.show();
	}

	public static void showNecessaryDialogForDeepLinkAction(final Context context, String message) {
		MaterialDialog dialog = new MaterialDialog.Builder(context)
				.cancelable(false)
				.title(android.R.string.dialog_alert_title)
				.content(message)
				.positiveText(android.R.string.ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						if (context instanceof AppCompatActivity)
							((AppCompatActivity) context).finishAffinity();
					}
				})
				.build();
		dialog.show();
	}
}