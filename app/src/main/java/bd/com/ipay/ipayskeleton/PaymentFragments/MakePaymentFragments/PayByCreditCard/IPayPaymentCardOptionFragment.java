package bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.PayByCreditCard;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.CardPaymentWebViewActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.PayByCardWebViewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByCreditOrDebitCardRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByCreditOrDebitCardResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.CardType;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card.GetSavedCardResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card.PaymentRequestAmex;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card.SavedCardInfo;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetSavedCardsList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MyCard;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Card.IPayAddMoneyFromCardSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.CardSelectDialog;

public class IPayPaymentCardOptionFragment extends Fragment implements HttpResponseListener {

	private static final int CARD_PAYMENT_WEB_VIEW_REQUEST = 2001;
	public static final String TRANSACTION_AMOUNT_KEY = "TRANSACTION_AMOUNT";

	private HttpRequestGetAsyncTask mGetSavedCardsAsyncTask;
	protected HttpRequestPostAsyncTask httpRequestPostAsyncTask = null;
	protected CustomProgressDialog mCustomProgressDialog = null;
	List<CardType> cardTypes;
	RecyclerView addMoneyOptionRecyclerView;

	private int transactionType;
	private String name;
	private BigDecimal amount;
	private String mobileNumber;
	private String profilePicture;

	private Long sponsorAccountID;

	private String sponsorName;
	private String sponsorProfilePictureUrl;

	private String mAddressString;
	private Long mOutletId = null;

	private EditText mNoteEditText;
	private EditText mPinEditText;

	private String operatorCode;
	private int operatorType;

	private List<SavedCardInfo> cardList;
	private List<SavedCardInfo> visaCardList = new ArrayList<>();
	private List<SavedCardInfo> masterCardList = new ArrayList<>();
	private final Gson gson = new GsonBuilder()
			.create();
	private final NumberFormat balanceBreakDownFormat = NumberFormat.getNumberInstance(Locale.getDefault());

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
				if (getArguments().containsKey(Constants.OUTLET_ID)) {
					mOutletId = getArguments().getLong(Constants.OUTLET_ID);
				}
				sponsorAccountID = getArguments().getLong(Constants.SPONSOR_ACCOUNT_ID);
				sponsorName = getArguments().getString(Constants.SPONSOR_NAME);
				sponsorProfilePictureUrl = getArguments().getString(Constants.SPONSOR_PROFILE_PICTURE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_credit_card_option, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mCustomProgressDialog = new CustomProgressDialog(getContext());

		final Toolbar toolbar = view.findViewById(R.id.toolbar);
		addMoneyOptionRecyclerView = view.findViewById(R.id.add_money_option_recycler_view);

		if (getActivity() == null || !(getActivity() instanceof AppCompatActivity)) {
			return;
		}
		addMoneyOptionRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
				LinearLayoutManager.HORIZONTAL));

		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}

		attemptGetSavedCards();
	}

	private void attemptGetSavedCards() {
		if (mGetSavedCardsAsyncTask != null) {
			return;
		} else {
			String uri = Constants.BASE_URL_SM + "saved-cards";
			mGetSavedCardsAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SAVED_CARDS,
					uri, getContext(), this, false);
			mGetSavedCardsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			mCustomProgressDialog.show();
		}
	}

	private void performContinueAction() {
		final String requestJson = gson.toJson(new PaymentRequestAmex(ContactEngine.formatMobileNumberBD(mobileNumber),
				amount.toString(), null, null, mOutletId, 0.0, 0.0, "AMEX"));
		httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_CREDIT_DEBIT_CARD, Constants.BASE_URL_SM + "payment/card",
				requestJson, getActivity(), this, false);
		httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mCustomProgressDialog.show();
	}

    private void performVisa(String brand) {
        final String requestJson = gson.toJson(new PaymentRequestAmex(ContactEngine.formatMobileNumberBD(mobileNumber),
                amount.toString(), null, null, mOutletId, 0.0, 0.0, brand));
        httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_CREDIT_DEBIT_CARD, Constants.BASE_URL_SM + "payment/wo-card-token",
                requestJson, getActivity(), this, false);
        httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mCustomProgressDialog.show();
    }

	private void genarateCardType() {
		cardTypes = new ArrayList<>();
		CardType cardType = new CardType(Constants.VISA_CARD, Constants.VISA, R.drawable.visa);
		cardTypes.add(cardType);
		cardType = new CardType(Constants.MASTER_CARD, Constants.MASTERCARD, R.drawable.mastercard);
		cardTypes.add(cardType);
		cardType = new CardType(Constants.AMEX_CARD, Constants.AMEX, R.drawable.amex);
		cardTypes.add(cardType);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
			httpRequestPostAsyncTask = null;
			mGetSavedCardsAsyncTask = null;
			mCustomProgressDialog.dismiss();
		} else {
			switch (result.getApiCommand()) {
				case Constants.COMMAND_ADD_MONEY_FROM_CREDIT_DEBIT_CARD:
					httpRequestPostAsyncTask = null;
					mCustomProgressDialog.dismiss();
					final AddMoneyByCreditOrDebitCardResponse mAddMoneyByCreditOrDebitResponse = gson.fromJson(result.getJsonString(), AddMoneyByCreditOrDebitCardResponse.class);
					switch (result.getStatus()) {
						case Constants.HTTP_RESPONSE_STATUS_OK:
							Intent intent = new Intent(getActivity(), PayByCardWebViewActivity.class);
							intent.putExtra(Constants.CARD_PAYMENT_URL, mAddMoneyByCreditOrDebitResponse.getForwardUrl());
							startActivityForResult(intent, CARD_PAYMENT_WEB_VIEW_REQUEST);
							break;
						case Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST:
						case Constants.HTTP_RESPONSE_STATUS_NOT_ACCEPTABLE:
							if (getActivity() != null)
								Toaster.makeText(getActivity(), mAddMoneyByCreditOrDebitResponse.getMessage(), Toast.LENGTH_SHORT);
							break;
						default:
							if (getActivity() != null)
								Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
							break;
					}
					break;
				case Constants.COMMAND_GET_SAVED_CARDS:
					mGetSavedCardsAsyncTask = null;
					mCustomProgressDialog.dismiss();
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						String jsonString = result.getJsonString();
						GetSavedCardResponse getSavedCardsList = new Gson().fromJson(jsonString, GetSavedCardResponse.class);
						cardList = getSavedCardsList.getSavedCardInfos();

						if(cardList.size()>0){
							visaCardList = new ArrayList<>();
							masterCardList = new ArrayList<>();
							for (SavedCardInfo savedCardInfo : cardList){
								if(savedCardInfo.getCardBrand().equalsIgnoreCase(Constants.VISA))
									visaCardList.add(savedCardInfo);
								else
									masterCardList.add(savedCardInfo);
							}
						}

					}

					genarateCardType();
					CreditCardOptionAdapter mCardTypeAdapter = new CreditCardOptionAdapter (getContext() ,cardTypes);
					addMoneyOptionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
					addMoneyOptionRecyclerView.setAdapter(mCardTypeAdapter);
					break;

			}
		}

	}

	public class CreditCardOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private List<CardType> mCardType;
		Context context;


		public CreditCardOptionAdapter(Context context, List<CardType> mCardType) {
			this.context = context;
			this.mCardType = mCardType;
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v;
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pay_by_card_type, parent, false);
			return new CreditCardOptionAdapter.ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			try {
				CreditCardOptionAdapter.ViewHolder vh = (CreditCardOptionAdapter.ViewHolder) holder;
				vh.bindView(position);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getItemCount() {
			if (mCardType == null)
				return 0;
			else
				return mCardType.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private final View itemView;
			private ProfileImageView cardIcon;
			private TextView mNameTextView;
			private TextView mTotalCard;

			public ViewHolder(View itemView) {

				super(itemView);

				this.itemView = itemView;
				cardIcon = itemView.findViewById(R.id.card_icon);
				mNameTextView = itemView.findViewById(R.id.card_name);
				mTotalCard = itemView.findViewById(R.id.no_card);
				cardIcon.setBusinessLogoPlaceHolder();
			}

			public void bindView(final int position) {
				mNameTextView.setText(mCardType.get(position).getCardName());
				cardIcon.setProfilePicture(mCardType.get(position).getCardIconDrawable());
				mTotalCard.setVisibility(View.GONE);

				if(mCardType.get(position).getCardKey().equalsIgnoreCase(Constants.VISA)){
					if(visaCardList.size()>0){
						mTotalCard.setVisibility(View.VISIBLE);
						mTotalCard.setText(visaCardList.size() +" card");
					}
				}

				if(mCardType.get(position).getCardKey().equalsIgnoreCase(Constants.MASTERCARD)){
					if(masterCardList.size()>0){
						mTotalCard.setVisibility(View.VISIBLE);
						mTotalCard.setText(masterCardList.size() + " card");
					}
				}

				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						if(mCardType.get(position).getCardKey().equalsIgnoreCase(Constants.AMEX)){
							performContinueAction();
						}else {
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

							if(mCardType.get(position).getCardKey().equalsIgnoreCase(Constants.VISA)){
								bundle.putString("CARD_TYPE", Constants.VISA);

								if(visaCardList.size()>0){
									bundle.putSerializable("SAVED_LIST", (Serializable) visaCardList);
									if (getActivity() instanceof IPayTransactionActionActivity) {
										((IPayTransactionActionActivity) getActivity()).switchToSavedCardFragment(bundle, 4);
									}

								}else {
                                    bundle.putBoolean("IS_SAVED_CARD", false);
									if (getActivity() instanceof IPayTransactionActionActivity) {
										((IPayTransactionActionActivity) getActivity()).switchToPayByCardConfirmationFragment(bundle, 4);
									}
									//performVisa(Constants.VISA);
								}
							}
							else{
								bundle.putString("CARD_TYPE", Constants.MASTERCARD);
								if(masterCardList.size()>0){
									bundle.putSerializable("SAVED_LIST", (Serializable) masterCardList);
									if (getActivity() instanceof IPayTransactionActionActivity) {
										((IPayTransactionActionActivity) getActivity()).switchToSavedCardFragment(bundle, 4);
									}

								}else {
                                    bundle.putBoolean("IS_SAVED_CARD", false);
									if (getActivity() instanceof IPayTransactionActionActivity) {
										((IPayTransactionActionActivity) getActivity()).switchToPayByCardConfirmationFragment(bundle, 4);
									}
									//performVisa(Constants.MASTERCARD);
								}
							}

						}
						//notifyDataSetChanged();
					}
				});
			}

		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CARD_PAYMENT_WEB_VIEW_REQUEST:
				if (data != null) {
					final int transactionStatusCode = data.getIntExtra(Constants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD_STATUS, CardPaymentWebViewActivity.CARD_TRANSACTION_CANCELED);
					switch (transactionStatusCode) {
						case CardPaymentWebViewActivity.CARD_TRANSACTION_CANCELED:
							if (getActivity() != null)
								getActivity().finish();
							break;
						case CardPaymentWebViewActivity.CARD_TRANSACTION_FAILED:
							if (getActivity() != null)
								getActivity().finish();
							break;
						case CardPaymentWebViewActivity.CARD_TRANSACTION_SUCCESSFUL:
							ProfileInfoCacheManager.addSourceOfFund(true);
							Bundle bundle = new Bundle();
							bundle.putSerializable(TRANSACTION_AMOUNT_KEY, amount);
							if (getActivity() instanceof IPayTransactionActionActivity)
								((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayAddMoneyFromCardSuccessFragment(), bundle, 4, true);
							break;
					}
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}
}



