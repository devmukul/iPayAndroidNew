package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.Tracker;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public abstract class IPayAbstractTransactionConfirmationFragment extends Fragment implements HttpResponseListener {
	protected Tracker mTracker;

	private EditText pinEditText;
	private EditText noteEditText;
	private TextView transactionDescriptionTextView;
	private RoundedImageView transactionImageView;
	private TextView nameTextView;
	private TextView userNameTextView;
	private Button transactionConfirmationButton;
	protected final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
	protected OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;
	protected AnimatedProgressDialog customProgressDialog;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumIntegerDigits(2);
		if (getActivity() != null)
			mTracker = Utilities.getTracker(getActivity());
		customProgressDialog = new AnimatedProgressDialog(getActivity());
		customProgressDialog.setCancelable(false);
	}

	@Nullable
	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_transaction_confirmation, container, false);
	}

	@Override
	public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Toolbar toolbar = view.findViewById(R.id.toolbar);
		final View pinLayoutHolder = view.findViewById(R.id.pin_layout_holder);
		final View noteLayoutHolder = view.findViewById(R.id.note_layout_holder);
		final ScrollView scrollView = view.findViewById(R.id.scroll_view);
		transactionConfirmationButton = view.findViewById(R.id.transaction_confirmation_button);
		transactionDescriptionTextView = view.findViewById(R.id.transaction_description_text_view);
		transactionImageView = view.findViewById(R.id.profile_image_view);
		nameTextView = view.findViewById(R.id.name_text_view);
		userNameTextView = view.findViewById(R.id.user_name_text_view);
		pinEditText = view.findViewById(R.id.pin_edit_text);
		noteEditText = view.findViewById(R.id.note_edit_text);

		if (getActivity() instanceof AppCompatActivity && !(getActivity() instanceof UtilityBillPaymentActivity)) {
			((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
			ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(true);
			}
			getActivity().setTitle(R.string.empty_string);
		}

		pinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					int bottom = pinEditText.getBottom() + scrollView.getPaddingBottom();
					int sy = scrollView.getScrollY();
					int sh = scrollView.getHeight();
					int delta = bottom - (sy + sh);
					scrollView.smoothScrollBy(0, delta);
				}
			}
		});

		noteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					int bottom = scrollView.getChildAt(scrollView.getChildCount() - 1).getBottom();
					int sy = scrollView.getScrollY();
					int sh = scrollView.getHeight();
					int delta = bottom - (sy + sh);
					scrollView.smoothScrollBy(0, delta);
				}
			}
		});

		transactionConfirmationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (verifyInput()) {
					Utilities.hideKeyboard(getContext(), pinEditText.getVisibility() == View.VISIBLE ? pinEditText : noteEditText);
					performContinueAction();
				}
			}
		});

		if (isPinRequired())
			pinLayoutHolder.setVisibility(View.VISIBLE);
		else
			pinLayoutHolder.setVisibility(View.GONE);

		if (canUserAddNote())
			noteLayoutHolder.setVisibility(View.VISIBLE);
		else
			noteLayoutHolder.setVisibility(View.GONE);

		setupViewProperties();
	}

	protected void setTransactionDescription(CharSequence transactionDescription) {
		transactionDescriptionTextView.setText(transactionDescription, TextView.BufferType.SPANNABLE);
	}

	protected void setName(CharSequence name) {
		nameTextView.setText(name, TextView.BufferType.SPANNABLE);
	}

	protected void setUserName(CharSequence userName) {
		userNameTextView.setVisibility(View.VISIBLE);
		userNameTextView.setText(userName, TextView.BufferType.SPANNABLE);
	}

	protected void setTransactionConfirmationButtonTitle(CharSequence title) {
		transactionConfirmationButton.setText(title, TextView.BufferType.SPANNABLE);
	}

	protected void setTransactionImageResource(@SuppressWarnings("SameParameterValue") int imageResource) {
		if (getContext() != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				transactionImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), imageResource, getContext().getTheme()));
			} else {
				Glide.with(getContext()).load(imageResource)
						.asBitmap()
						.transform(new CircleTransform(getContext()))
						.crossFade()
						.error(R.drawable.ic_profile)
						.placeholder(R.drawable.ic_profile)
						.into(transactionImageView);
			}
		}
	}

	@SuppressWarnings("unused")
	protected void setTransactionImage(String imageUrl) {
		Glide.with(getContext()).load(imageUrl)
				.transform(new CircleTransform(getContext()))
				.crossFade()
				.error(R.drawable.ic_profile)
				.placeholder(R.drawable.ic_profile)
				.into(transactionImageView);
	}

	protected String getPin() {
		final Editable pin = pinEditText.getText();
		return pin != null ? pin.toString() : "";
	}

	protected String getNote() {
		final Editable note = noteEditText.getText();
		return !TextUtils.isEmpty(note) ? note.toString() : null;
	}

	protected final void sendSuccessEventTracking(Number amount) {
		Utilities.sendSuccessEventTracker(mTracker, getTrackerCategory(), ProfileInfoCacheManager.getAccountId(), amount.longValue());
	}

	protected final void sendFailedEventTracking(@NonNull String failMessage, @NonNull Number amount) {
		Utilities.sendFailedEventTracker(mTracker, getTrackerCategory(), ProfileInfoCacheManager.getAccountId(),
				failMessage, amount.longValue());
	}

	protected final void sendBlockedEventTracking(@NonNull Number amount) {
		Utilities.sendBlockedEventTracker(mTracker, getTrackerCategory(), ProfileInfoCacheManager.getAccountId(), amount.longValue());
	}

	protected void setNoteEditTextHint(CharSequence hint) {
		noteEditText.setVisibility(View.VISIBLE);
		noteEditText.setHint(hint);
	}

	protected void showErrorMessage(String errorMessage) {
		if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
			IPaySnackbar.error(transactionConfirmationButton, errorMessage, IPaySnackbar.LENGTH_SHORT).show();
		}
	}

	protected CharSequence getStyledTransactionDescription(@StringRes int transactionStringId, Number amount) {
		final String amountValue = numberFormat.format(amount);
		final String spannedString = getString(transactionStringId, amountValue);
		final String formattedAmountString = getString(R.string.balance_holder, amountValue);
		int position = spannedString.indexOf(formattedAmountString);
		final Spannable spannableAmount = new SpannableString(getString(transactionStringId, amountValue));
		spannableAmount.setSpan(new StyleSpan(Typeface.BOLD), position, position + formattedAmountString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannableAmount.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorLightGreenSendMoney)), position, position + formattedAmountString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableAmount;
	}

	protected void launchOTPVerification(long otpValidFor, String requestJson, String apiCommand, String url) {
		if (getActivity() != null) {
			mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), requestJson, apiCommand,
					url, Constants.METHOD_POST, otpValidFor);
			mOTPVerificationForTwoFactorAuthenticationServicesDialog.setOtpValidFor(otpValidFor);
			mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;

		}
	}

	protected abstract void setupViewProperties();

	protected abstract boolean isPinRequired();

	protected abstract boolean canUserAddNote();

	protected abstract String getTrackerCategory();

	protected abstract boolean verifyInput();

	protected abstract void performContinueAction();
}
