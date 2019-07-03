package bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.PayByCreditCard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card.SavedCardInfo;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GroupedScheduledPaymentList;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayPayByCardSavedCardFragment extends Fragment {

	private int transactionType;
	private String name;
	private BigDecimal amount;
	private String mobileNumber;
	private String profilePicture;

	private Long sponsorAccountID;

	private String sponsorName;
	private String sponsorProfilePictureUrl;

	private String mAddressString;

	private String mCardType;
	private Long mOutletId = null;

	private EditText mNoteEditText;
	private EditText mPinEditText;

	private String operatorCode;
	private int operatorType;

	private List<SavedCardInfo> cardList;
    private CustomProgressDialog progressDialog;

    private TextView emptyTextView;


    private RecyclerView scheduledPaymentListRecyclerView;
    private ScheduledPaymentListAdapter scheduledPaymentAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sponsorAccountID = (long) -1;
		try {
			if (getArguments() != null) {
				transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY);
				name = getArguments().getString(Constants.NAME);
				mobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);
				profilePicture = getArguments().getString(Constants.PHOTO_URI);
				amount = (BigDecimal) getArguments().getSerializable(Constants.AMOUNT);
				mAddressString = getArguments().getString(Constants.ADDRESS);
				mCardType = getArguments().getString("CARD_TYPE");
				if (getArguments().containsKey(Constants.OUTLET_ID)) {
					mOutletId = getArguments().getLong(Constants.OUTLET_ID);
				}
				sponsorAccountID = getArguments().getLong(Constants.SPONSOR_ACCOUNT_ID);
				sponsorName = getArguments().getString(Constants.SPONSOR_NAME);
				sponsorProfilePictureUrl = getArguments().getString(Constants.SPONSOR_PROFILE_PICTURE);


                cardList = (List<SavedCardInfo>) getArguments().getSerializable("SAVED_LIST");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_seved_card, container, false);

		final Toolbar toolbar = v.findViewById(R.id.toolbar);

		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}

        scheduledPaymentListRecyclerView = v.findViewById(R.id.scheduled_payment_list);

        emptyTextView = v.findViewById(R.id.empty_text);
        progressDialog = new CustomProgressDialog(getContext());

        scheduledPaymentAdapter = new ScheduledPaymentListAdapter(cardList);
        scheduledPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduledPaymentListRecyclerView.setAdapter(scheduledPaymentAdapter);
        scheduledPaymentAdapter.notifyDataSetChanged();

		Button otherCard = v.findViewById(R.id.other);
		otherCard.setOnClickListener(new View.OnClickListener() {
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
				bundle.putString("CARD_TYPE", mCardType);
				bundle.putBoolean("IS_SAVED_CARD", false);
				if (mOutletId != null)
					bundle.putLong(Constants.OUTLET_ID, mOutletId);
				bundle.putSerializable(Constants.AMOUNT, amount);

				if (getActivity() instanceof IPayTransactionActionActivity) {
					((IPayTransactionActionActivity) getActivity()).switchToPayByCardConfirmationFragment(bundle, 5);
				}

			}
		});

		return v;
	}



    public class ScheduledPaymentListAdapter extends RecyclerView.Adapter<ScheduledPaymentListAdapter.ScheduledPaymentViewHolder> {
        private List<SavedCardInfo> scheduledPaymentInfoList;

        public ScheduledPaymentListAdapter(List<SavedCardInfo> scheduledPaymentInfoList) {
            this.scheduledPaymentInfoList = scheduledPaymentInfoList;
        }


        @NonNull
        @Override
        public ScheduledPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ScheduledPaymentViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.list_item_card_type, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, final int i) {

            scheduledPaymentViewHolder.mNameTextView.setText(scheduledPaymentInfoList.get(i).getMaskedCardNumber());
            if(mCardType.equalsIgnoreCase(Constants.VISA))
            	scheduledPaymentViewHolder.cardIcon.setProfilePicture(R.drawable.visa);
            else
				scheduledPaymentViewHolder.cardIcon.setProfilePicture(R.drawable.mastercard);

            scheduledPaymentViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
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
					bundle.putString("CARD_TYPE", mCardType);
					bundle.putString("CARD_ID", scheduledPaymentInfoList.get(i).getCardId());
					bundle.putBoolean("IS_SAVED_CARD", true);
					if (mOutletId != null)
						bundle.putLong(Constants.OUTLET_ID, mOutletId);
					bundle.putSerializable(Constants.AMOUNT, amount);

					if (getActivity() instanceof IPayTransactionActionActivity) {
						((IPayTransactionActionActivity) getActivity()).switchToPayByCardConfirmationFragment(bundle, 5);
					}
                }
            });
        }

        @Override
        public int getItemCount() {
            if (this.scheduledPaymentInfoList == null) return 0;
            else return this.scheduledPaymentInfoList.size();
        }

        public class ScheduledPaymentViewHolder extends RecyclerView.ViewHolder {
            private final View itemView;
            private ProfileImageView cardIcon;
            private TextView mNameTextView;

            public ScheduledPaymentViewHolder(View itemView) {

                super(itemView);

                this.itemView = itemView;
                cardIcon = (ProfileImageView) itemView.findViewById(R.id.card_icon);
                mNameTextView = (TextView) itemView.findViewById(R.id.card_name);
                cardIcon.setBusinessLogoPlaceHolder();
            }
        }
    }

	private void launchAddBankAgreementPage(BankAccountList bankAccountList) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.BANK_NAME, bankAccountList.getBankName());
		bundle.putString(Constants.BANK_BRANCH_NAME, bankAccountList.getBranchName());
		bundle.putString(Constants.BANK_BRANCH_ROUTE_NO, bankAccountList.getBranchRoutingNumber());
		bundle.putBoolean(Constants.FROM_ON_BOARD, false);
		bundle.putString(Constants.BANK_ACCOUNT_NAME, bankAccountList.getAccountName());
		bundle.putString(Constants.BANK_ACCOUNT_NUMBER, bankAccountList.getAccountNumber());
		bundle.putBoolean(Constants.IS_STARTED_FROM_PROFILE_COMPLETION, false);
		bundle.putBoolean(Constants.IS_STARTED_FROM_UNCONCENTED_LIST, true);
		bundle.putLong(Constants.BANK_ACCOUNT_ID, bankAccountList.getBankAccountId());

		((ManageBanksActivity) getActivity()).switchToAddBankAgreementFragment(bundle);
	}
}