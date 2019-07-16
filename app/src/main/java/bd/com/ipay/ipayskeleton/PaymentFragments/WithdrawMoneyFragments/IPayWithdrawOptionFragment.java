package bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.IsInstantResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.CreditBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
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

public class IPayWithdrawOptionFragment extends Fragment {

	private CreditBalanceResponse creditBalanceResponse;
	private HttpRequestGetAsyncTask httpRequestGetAsyncTask;
	private TextView addMoneyBankOptionMessageTextView;
	private AnimatedProgressDialog customProgressDialog;
    private String cardType;
	private final Gson gson = new GsonBuilder()
			.create();
	private final NumberFormat balanceBreakDownFormat = NumberFormat.getNumberInstance(Locale.getDefault());

	protected BankAccountList bankAccountList;
	protected IsInstantResponse isInstantResponse;

	CardView instantWithdraw;
	CardView lazyWithdraw;
	TextView instantMsg;
	TextView lazyMsg;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			bankAccountList = getArguments().getParcelable(Constants.SELECTED_BANK_ACCOUNT);
			isInstantResponse = getArguments().getParcelable("INSTANT");
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_withdraw_money_option, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Toolbar toolbar = view.findViewById(R.id.toolbar);
		if (getActivity() == null || !(getActivity() instanceof AppCompatActivity)) {
			return;
		}

		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}

		instantWithdraw = view.findViewById(R.id.instant_withdraw);
		lazyWithdraw = view.findViewById(R.id.lazy_withdraw);
		instantMsg = view.findViewById(R.id.instant_description_text_view);
		lazyMsg = view.findViewById(R.id.lazy_description_text_view);

		if(!isInstantResponse.getInstant().getIsEligible()){
			instantWithdraw.setVisibility(View.GONE);
		}

		if(!isInstantResponse.getLazy().getIsEligible()){
			lazyWithdraw.setVisibility(View.GONE);
		}

		if(bankAccountList.getBankCode().equals("060")){
			if(bankAccountList.getBranchName().equalsIgnoreCase("INTERNET")){
				lazyWithdraw.setVisibility(View.GONE);
			}
		}

		instantMsg.setText(isInstantResponse.getInstant().getFeeDescription());
		lazyMsg.setText(isInstantResponse.getLazy().getFeeDescription());

		lazyWithdraw.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle mBundle = new Bundle();
				mBundle.putParcelable(Constants.SELECTED_BANK_ACCOUNT, bankAccountList);
				mBundle.putBoolean("IS_INSTANT", false);
				mBundle.putLong("FLAT_FEE", isInstantResponse.getLazy().getFeeInfo().getFlatFee());
				mBundle.putLong("VARIABLE_FEE", isInstantResponse.getLazy().getFeeInfo().getVariableFee());
				mBundle.putLong("MAX_FEE", isInstantResponse.getLazy().getFeeInfo().getMaxFee());
				((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayWithdrawMoneyFromBankAmountInputFragment(), mBundle, 1, true);

			}
		});

		instantWithdraw.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle mBundle = new Bundle();
				mBundle.putParcelable(Constants.SELECTED_BANK_ACCOUNT, bankAccountList);
				mBundle.putBoolean("IS_INSTANT", true);
				mBundle.putLong("FLAT_FEE", isInstantResponse.getInstant().getFeeInfo().getFlatFee());
				mBundle.putLong("VARIABLE_FEE", isInstantResponse.getInstant().getFeeInfo().getVariableFee());
				mBundle.putLong("MAX_FEE", isInstantResponse.getInstant().getFeeInfo().getMaxFee());
				((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayWithdrawMoneyFromBankAmountInputFragment(), mBundle, 1, true);

			}
		});

	}

}
