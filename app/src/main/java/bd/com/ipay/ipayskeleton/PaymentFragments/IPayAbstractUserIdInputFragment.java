package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public abstract class IPayAbstractUserIdInputFragment extends Fragment {

	private RoundedImageView merchantIconImageView;
	private TextView inputMessageTextView;
	private EditText userIdEditText;
	private Button continueButton;
	private View otherPersionMobileView;
	private View otherPersionNameView;
	private EditText otherPersionMobileEditText;
	private EditText otherPersionNameEditText;
	private CheckBox isOtherPerson;

	@Nullable
	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_user_id_input, container, false);
	}

	@Override
	public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Toolbar toolbar = view.findViewById(R.id.toolbar);
		continueButton = view.findViewById(R.id.button_send_money);
		merchantIconImageView = view.findViewById(R.id.merchant_icon_image_view);
		inputMessageTextView = view.findViewById(R.id.input_message_text_view);
		userIdEditText = view.findViewById(R.id.user_id_edit_text);
		otherPersionMobileView = view.findViewById(R.id.other_person_mobile_view);
		otherPersionNameView = view.findViewById(R.id.other_person_name_view);
		otherPersionMobileEditText = view.findViewById(R.id.other_person_mobile_edit_text);
		otherPersionNameEditText = view.findViewById(R.id.other_person_name_edit_text);
		isOtherPerson = view.findViewById(R.id.paying_for_other_option);

		isOtherPerson.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

				if (b){
					otherPersionNameView.setVisibility(View.VISIBLE);
					otherPersionMobileView.setVisibility(View.VISIBLE);
				}else{
					otherPersionNameView.setVisibility(View.GONE);
					otherPersionMobileView.setVisibility(View.GONE);
				}

			}
		});


		if (getActivity() instanceof AppCompatActivity) {
			((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
			final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
			if (actionBar != null)
				actionBar.setDisplayHomeAsUpEnabled(true);
		}

		continueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (verifyInput()) {
					performContinueAction();
				}
			}
		});
		setupViewProperties();
	}

	protected void setTitle(CharSequence title) {
		if (getActivity() != null) {
			getActivity().setTitle(title);
		}
	}

	protected void setMerchantIconImage(int imageSource) {
		merchantIconImageView.setImageResource(imageSource);
	}

	protected void setMerchantIconImage(String imageUrl) {
		Glide.with(getActivity())
				.load(imageUrl)
				.transform(new CircleTransform(getActivity()))
				.crossFade()
				.into(merchantIconImageView);
	}

	protected void setInputMessage(CharSequence inputMessage) {
		inputMessageTextView.setVisibility(View.VISIBLE);
		inputMessageTextView.setText(inputMessage, TextView.BufferType.SPANNABLE);
	}

	protected void setUserIdHint(CharSequence userIdHint) {
		userIdEditText.setHint(userIdHint);
	}

	protected String getUserId() {
		return TextUtils.isEmpty(userIdEditText.getText()) ? null : userIdEditText.getText().toString().trim();
	}

	protected void setUserId(CharSequence userId) {
        userIdEditText.setText(userId);
	}

	protected void setOtherPersonChecked(boolean isChecked){
		isOtherPerson.setChecked(isChecked);
	}

	protected boolean ifPayingForOtherPerson() {
        return isOtherPerson.isChecked();
    }

	protected void hidePayingForOtherPerson() {
		isOtherPerson.setVisibility(View.GONE);
	}

	protected void setOtherPersonName(CharSequence inputMessag) {
		otherPersionNameEditText.setText(inputMessag);
	}

	protected void setOtherPersonMobile(CharSequence inputMessag) {
		otherPersionMobileEditText.setText(inputMessag);
	}

    protected String getOtherPersonName() {
        return TextUtils.isEmpty(otherPersionNameEditText.getText()) ? null : otherPersionNameEditText.getText().toString().trim();
    }

    protected String getOtherPersonMobile() {
        return TextUtils.isEmpty(otherPersionMobileEditText.getText()) ? null : otherPersionMobileEditText.getText().toString().trim();
    }

	protected void showErrorMessage(String errorMessage) {
		if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
			IPaySnackbar.error(continueButton, errorMessage, IPaySnackbar.LENGTH_SHORT).show();
		}
	}

	protected abstract boolean verifyInput();

	protected abstract void performContinueAction();

	protected abstract void setupViewProperties();
}
