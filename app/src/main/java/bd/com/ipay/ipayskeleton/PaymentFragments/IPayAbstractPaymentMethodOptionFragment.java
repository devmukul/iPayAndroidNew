package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments.IPayWithdrawMoneyFromBankAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.SourceOfFund.view.SponsorSelectorDialog;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;

public class IPayAbstractPaymentMethodOptionFragment extends Fragment {

	private int transactionType;

	private TextView headerTextView;
	private TextView messageTextView;

	CardView iPayWallet;
	CardView creditCard;


	private String name;
	private BigDecimal amount;
	private String mobileNumber;
	private String profilePicture;

	private Long sponsorAccountID;

	private String sponsorName;
	private String sponsorProfilePictureUrl;

	private String mAddressString;
	private Long mOutletId = null;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID);

			name = getArguments().getString(Constants.NAME);
			mobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);
			profilePicture = getArguments().getString(Constants.PHOTO_URI);
			amount = (BigDecimal) getArguments().getSerializable(Constants.AMOUNT);
			mAddressString = getArguments().getString(Constants.ADDRESS);
			if (getArguments().containsKey(Constants.OUTLET_ID)) {
				mOutletId = getArguments().getLong(Constants.OUTLET_ID);
			}
			if (transactionType == ServiceIdConstants.MAKE_PAYMENT) {
				sponsorAccountID = getArguments().getLong(Constants.SPONSOR_ACCOUNT_ID);
				sponsorName = getArguments().getString(Constants.SPONSOR_NAME);
				sponsorProfilePictureUrl = getArguments().getString(Constants.SPONSOR_PROFILE_PICTURE);
			}
		}
	}

	@Nullable
	@Override
	public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_payment_method_option, container, false);
	}

	@Override
	public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Toolbar toolbar = view.findViewById(R.id.toolbar);
		headerTextView = view.findViewById(R.id.header_text_view);
		messageTextView = view.findViewById(R.id.message_text_view);

		if (getActivity() == null || !(getActivity() instanceof AppCompatActivity) || transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID) {
			return;
		}

		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}


		iPayWallet = view.findViewById(R.id.pay_by_ipay);
		creditCard = view.findViewById(R.id.pay_by_credit_card);


		creditCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
				bundle.putString(Constants.NAME, name);
				bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
				bundle.putString(Constants.PHOTO_URI, profilePicture);
				bundle.putString(Constants.ADDRESS, mAddressString);
				bundle.putLong(Constants.SPONSOR_ACCOUNT_ID, sponsorAccountID);
				bundle.putString(Constants.SPONSOR_NAME, sponsorName);
				bundle.putString(Constants.SPONSOR_PROFILE_PICTURE, sponsorProfilePictureUrl);
				if (mOutletId != null)
					bundle.putLong(Constants.OUTLET_ID, mOutletId);
				bundle.putSerializable(Constants.AMOUNT, amount);
				if (getActivity() instanceof IPayTransactionActionActivity) {
					((IPayTransactionActionActivity) getActivity()).switchToCardTypeFragment(bundle);
				}

			}
		});

		iPayWallet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Bundle bundle = new Bundle();
				bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
				bundle.putString(Constants.NAME, name);
				bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
				bundle.putString(Constants.PHOTO_URI, profilePicture);
				bundle.putString(Constants.ADDRESS, mAddressString);
				bundle.putLong(Constants.SPONSOR_ACCOUNT_ID, sponsorAccountID);
				bundle.putString(Constants.SPONSOR_NAME, sponsorName);
				bundle.putString(Constants.SPONSOR_PROFILE_PICTURE, sponsorProfilePictureUrl);
				if (mOutletId != null)
					bundle.putLong(Constants.OUTLET_ID, mOutletId);
				bundle.putSerializable(Constants.AMOUNT, amount);
				if (getActivity() instanceof IPayTransactionActionActivity) {
					((IPayTransactionActionActivity) getActivity()).switchToTransactionConfirmationFragment(bundle);
				}
			}
		});
	}


	protected void setHeaderText(CharSequence headerText) {
		headerTextView.setText(headerText, TextView.BufferType.SPANNABLE);
	}

	protected void setMessageText(CharSequence messageText) {
		messageTextView.setText(messageText, TextView.BufferType.SPANNABLE);
	}
}
