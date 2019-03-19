package bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Adapters.AddMoneyOptionAdapter;
import bd.com.ipay.ipayskeleton.Adapters.OnItemClickListener;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.Model.AddMoneyOption;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.CreditBalanceResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Card.IPayAddMoneyFromCardAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayChooseBankOptionFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.CardSelectDialog;

public class IPayAddMoneyOptionFragment extends Fragment {

	private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
	private CreditBalanceResponse creditBalanceResponse;
	private HttpRequestGetAsyncTask httpRequestGetAsyncTask;
	private TextView bottomSheetTitleTextView;
	private TextView addMoneyBankOptionMessageTextView;
	private AnimatedProgressDialog customProgressDialog;
    private String cardType;
	private final Gson gson = new GsonBuilder()
			.create();
	private final NumberFormat balanceBreakDownFormat = NumberFormat.getNumberInstance(Locale.getDefault());

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_add_money_option, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Toolbar toolbar = view.findViewById(R.id.toolbar);
		final RecyclerView addMoneyOptionRecyclerView = view.findViewById(R.id.add_money_option_recycler_view);
		final List<AddMoneyOption> addMoneyOptionList = Utilities.getAddMoneyOptions();
		final LinearLayout bankListBottomSheetLayout = view.findViewById(R.id.bank_option_bottom_sheet_layout);
		final ImageButton closeAddBankOptionButton = view.findViewById(R.id.close_add_bank_option_button);
		bottomSheetTitleTextView = view.findViewById(R.id.bottom_sheet_title_text_view);
		addMoneyBankOptionMessageTextView = view.findViewById(R.id.add_money_bank_option_message_text_view);
		bottomSheetBehavior = BottomSheetBehavior.from(bankListBottomSheetLayout);

		balanceBreakDownFormat.setMinimumFractionDigits(2);
		balanceBreakDownFormat.setMaximumFractionDigits(2);

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
		final AddMoneyOptionAdapter addMoneyOptionAdapter = new AddMoneyOptionAdapter(getActivity(), new OnItemClickListener() {
			@Override
			public void onItemClick(int position, View view) {
				final AddMoneyOption selectedAddMoneyOption = addMoneyOptionList.get(position);
				switch (selectedAddMoneyOption.getServiceId()) {
					case ServiceIdConstants.ADD_MONEY_BY_BANK:
						showBankList(IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY_BY_BANK);
						break;
					case ServiceIdConstants.ADD_MONEY_BY_BANK_INSTANTLY:
						if (creditBalanceResponse != null && httpRequestGetAsyncTask == null) {
							if (creditBalanceResponse.isEntitledForInstantMoney()) {
								showBankList(IPayTransactionActionActivity.
										TRANSACTION_TYPE_ADD_MONEY_BY_BANK_INSTANTLY);
							} else {
								DialogUtils.showDialogForNotEntitledForInstantMoney(getActivity());
							}
						} else if (httpRequestGetAsyncTask != null) {
							customProgressDialog = new AnimatedProgressDialog(getActivity());
							customProgressDialog.showDialog();
						} else {
							Toaster.makeText(getActivity(),
									R.string.service_not_available, Toast.LENGTH_SHORT);
						}

						break;
					case ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD:
						showCardType();
						break;
				}
			}
		});
		addMoneyOptionAdapter.setItemList(addMoneyOptionList);
		addMoneyOptionRecyclerView.setAdapter(addMoneyOptionAdapter);

		closeAddBankOptionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
					bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
					if (getFragmentManager() != null)
						getFragmentManager().beginTransaction().remove(addBankOptionFragment).commit();
				}
			}
		});

		fetchCreditBalance();
	}

	private void fetchCreditBalance() {
		httpRequestGetAsyncTask = new HttpRequestGetAsyncTask(
				Constants.COMMAND_ADD_MONEY_FROM_BANK_INSTANTLY_BALANCE,
				Constants.BASE_URL_SM + Constants.URL_ADD_MONEY_FROM_BANK_INSTANTLY_BALANCE,
				getActivity(),
				new HttpResponseListener() {
					@Override
					public void httpResponseReceiver(GenericHttpResponse result) {
						httpRequestGetAsyncTask = null;
						if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
							creditBalanceResponse =
									gson.fromJson(result.getJsonString(),
											CreditBalanceResponse.class);
							SharedPrefManager.setCreditBalance(creditBalanceResponse);
							if (customProgressDialog != null) {
								customProgressDialog.dismissDialog();
								if (creditBalanceResponse.isEntitledForInstantMoney()) {
									showBankList(IPayTransactionActionActivity
											.TRANSACTION_TYPE_ADD_MONEY_BY_BANK_INSTANTLY);
								} else {
									DialogUtils
											.showDialogForNotEntitledForInstantMoney(getContext());
								}
							}
						} else {
							if (customProgressDialog != null) {
								customProgressDialog.dismissDialog();
								Toaster.makeText(getContext(),
										R.string.service_not_available,
										Toast.LENGTH_SHORT);
							}
						}
					}
				}, true);
		httpRequestGetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void showBankList(int transactionType) {
		if (bottomSheetBehavior.getState() ==
				BottomSheetBehavior.STATE_COLLAPSED ||
				bottomSheetBehavior.getState() ==
						BottomSheetBehavior.STATE_HIDDEN) {
			bottomSheetBehavior
					.setState(BottomSheetBehavior.STATE_EXPANDED);
			addBankOptionFragment = new IPayChooseBankOptionFragment();
			Bundle bundle = new Bundle();
			switch (transactionType) {
				case IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY_BY_BANK:
					addMoneyBankOptionMessageTextView.setVisibility(View.GONE);
					bottomSheetTitleTextView.setText(R.string.adding_money_from_bank);
					break;
				case IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY_BY_BANK_INSTANTLY:
					if (creditBalanceResponse != null) {
						addMoneyBankOptionMessageTextView.setVisibility(View.VISIBLE);
						addMoneyBankOptionMessageTextView.setText(getString(R.string.instant_money_message_alert,
								balanceBreakDownFormat.format(creditBalanceResponse.getCreditLimit())));
					} else {
						addMoneyBankOptionMessageTextView.setVisibility(View.GONE);
					}
					bottomSheetTitleTextView.setText(R.string.instant_money);
					break;
			}
			bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY,
					transactionType);
			addBankOptionFragment.setArguments(bundle);
			if (getFragmentManager() != null)
				getFragmentManager().beginTransaction()
						.replace(R.id.bank_option_fragment_container, addBankOptionFragment)
						.commit();
		}
	}

	private Fragment addBankOptionFragment;

	public boolean onBackPressed() {
		if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
			if (getFragmentManager() != null)
				getFragmentManager().beginTransaction()
						.remove(addBankOptionFragment)
						.commit();
			return true;
		} else {
			return false;
		}
	}

	private void showCardType() {
		if (getActivity() == null)
			return;

		final CardSelectDialog cardSelectDialog = new CardSelectDialog(getContext());
		cardSelectDialog.setTitle(getString(R.string.select_card_type));
		cardSelectDialog.setCloseButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cardSelectDialog.cancel();
			}
		});
		cardSelectDialog.setPayBillButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                cardType = cardSelectDialog.getSelectedCardType();
                if(!TextUtils.isEmpty(cardType)) {
                    cardSelectDialog.cancel();
                    BusinessRuleCacheManager.fetchBusinessRule(getContext(), IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.CARD_TYPE, cardType);
                    if (getActivity() instanceof IPayTransactionActionActivity) {
                        ((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayAddMoneyFromCardAmountInputFragment(), bundle, 1, true);
                    }
                }else{
                    Toaster.makeText(getContext(), "Please select a card!", Toast.LENGTH_LONG);
                }
			}
		});
        cardSelectDialog.show();
	}
}
