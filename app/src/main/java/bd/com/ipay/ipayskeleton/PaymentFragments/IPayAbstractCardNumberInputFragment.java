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
import android.widget.ImageView;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Widget.View.CardNumberEditText;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public abstract class IPayAbstractCardNumberInputFragment extends Fragment {

	private ImageView cardIconImageView;
	private TextView cardMessageTextView;
	private CardNumberEditText cardNumberEditText;
	private Button continueButton;

	private View otherPersionMobileView;
	private View otherPersionNameView;
	private EditText otherPersionMobileEditText;
	private EditText otherPersionNameEditText;
	private CheckBox isOtherPerson;

	@Nullable
	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_card_number_input, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Toolbar toolbar = view.findViewById(R.id.toolbar);
		cardIconImageView = view.findViewById(R.id.card_icon_image_view);
		cardMessageTextView = view.findViewById(R.id.card_message_image_view);
		cardNumberEditText = view.findViewById(R.id.card_number_edit_text);
		continueButton = view.findViewById(R.id.button_send_money);

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
	}

	public void setCardNumberHint(String hint) {
		if (!TextUtils.isEmpty(hint)) {
			cardNumberEditText.setHint(hint);
		}
	}

	public void setAllowedCards(CardNumberValidator.Cards... allowedCards) {
		cardNumberEditText.setAllowedCards(allowedCards);
	}

	public CardNumberValidator.Cards[] getAllowedCards() {
		return cardNumberEditText.getAllowedCards();
	}

	public String getCardNumber() {
		if (cardNumberEditText.getText() != null)
			return cardNumberEditText.getText().toString();
		else
			return "";
	}


    protected void setCardNumber(CharSequence userId) {
        cardNumberEditText.setText(userId);
    }


    public void setCardIconImageResource(int imageResource) {
		cardIconImageView.setImageResource(imageResource);
	}

	public void setMessage(String message) {
		if (TextUtils.isEmpty(message)) {
			cardMessageTextView.setVisibility(View.GONE);
			cardMessageTextView.setText("");
		} else {
			cardMessageTextView.setVisibility(View.VISIBLE);
			cardMessageTextView.setText(message);
		}
	}

    protected void setOtherPersonChecked(boolean isChecked){
        isOtherPerson.setChecked(isChecked);
    }

    protected boolean ifPayingForOtherPerson() {
        return isOtherPerson.isChecked();
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
}
