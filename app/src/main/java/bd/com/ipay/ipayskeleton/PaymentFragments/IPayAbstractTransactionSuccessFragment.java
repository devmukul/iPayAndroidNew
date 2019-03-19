package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;

public abstract class IPayAbstractTransactionSuccessFragment extends Fragment {

	private TextView transactionSuccessMessageTextView;
	private TextView successDescriptionTextView;
	private TextView nameTextView;
	private TextView userNameTextView;
	private RoundedImageView senderProfilePictureImageView;
	private RoundedImageView receiverProfilePictureImageView;
	private ImageView arrowImageView;
	protected final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumIntegerDigits(2);
	}

	@Nullable
	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_transaction_success, container, false);
	}

	@Override
	public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Button goToWalletButton = view.findViewById(R.id.go_to_wallet_button);
		transactionSuccessMessageTextView = view.findViewById(R.id.transaction_success_message_text_view);
		successDescriptionTextView = view.findViewById(R.id.success_description_text_view);
		nameTextView = view.findViewById(R.id.name_text_view);
		userNameTextView = view.findViewById(R.id.user_name_text_view);
		senderProfilePictureImageView = view.findViewById(R.id.sender_profile_picture_image_view);
		receiverProfilePictureImageView = view.findViewById(R.id.receiver_profile_picture_image_view);
		arrowImageView = view.findViewById(R.id.arrow_icon_image_view);

		senderProfilePictureImageView.setVisibility(View.GONE);
		arrowImageView.setVisibility(View.GONE);

		goToWalletButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() != null) {
					getActivity().setResult(Activity.RESULT_OK);
					getActivity().finish();
				}
			}
		});

		setupViewProperties();
	}

	protected abstract void setupViewProperties();

	protected void setTransactionSuccessMessage(CharSequence transactionSuccessMessage) {
		transactionSuccessMessageTextView.setText(transactionSuccessMessage, TextView.BufferType.SPANNABLE);
	}

	protected void setName(CharSequence name) {
		nameTextView.setText(name, TextView.BufferType.SPANNABLE);
	}

	protected void setUserName(CharSequence userName) {
		userNameTextView.setVisibility(View.VISIBLE);
		userNameTextView.setText(userName, TextView.BufferType.SPANNABLE);
	}

	protected void setSuccessDescription(CharSequence successDescription) {
		successDescriptionTextView.setText(successDescription, TextView.BufferType.SPANNABLE);
	}

	@SuppressWarnings("unused")
	protected void setSenderImage(int imageResource) {
		arrowImageView.setVisibility(View.VISIBLE);
		senderProfilePictureImageView.setVisibility(View.VISIBLE);
		if (getContext() != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				senderProfilePictureImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), imageResource, getContext().getTheme()));
			} else {
				Glide.with(getContext()).load(imageResource)
						.asBitmap()
						.transform(new CircleTransform(getContext()))
						.crossFade()
						.error(R.drawable.ic_profile)
						.placeholder(R.drawable.ic_profile)
						.into(senderProfilePictureImageView);
			}
		}
	}

	protected void setSenderImage(String imageUrl) {
		arrowImageView.setVisibility(View.VISIBLE);
		senderProfilePictureImageView.setVisibility(View.VISIBLE);
		Glide.with(getContext()).load(imageUrl)
				.transform(new CircleTransform(getContext()))
				.crossFade()
				.error(R.drawable.ic_profile)
				.placeholder(R.drawable.ic_profile)
				.into(senderProfilePictureImageView);
	}

	protected void setReceiverImage(int imageResource) {
		if (getContext() != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				receiverProfilePictureImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), imageResource, getContext().getTheme()));
			} else {
				Glide.with(getContext()).load(imageResource)
						.asBitmap()
						.transform(new CircleTransform(getContext()))
						.crossFade()
						.error(R.drawable.ic_profile)
						.placeholder(R.drawable.ic_profile)
						.into(receiverProfilePictureImageView);
			}
		}
	}

	protected void setReceiverImage(String imageUrl) {
		Glide.with(getContext()).load(imageUrl)
				.transform(new CircleTransform(getContext()))
				.crossFade()
				.error(R.drawable.ic_profile)
				.placeholder(R.drawable.ic_profile)
				.into(receiverProfilePictureImageView);
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
}
