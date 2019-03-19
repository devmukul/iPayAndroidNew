package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddCardResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.CardDetails;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BankBranch;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Card.IPayAddMoneyFromCardAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.CardSelectDialog;

public class AddCardActivity extends BaseActivity implements HttpResponseListener {

	private static final int INVALID_RESOURCE_ID = 0;
	private HttpRequestGetAsyncTask mGetAllAddedCards;

	private List<CardDetails> mCardList;

	public FloatingActionButton mFabAddNewBank;
	private TextView mDescriptionTextView;

	private CustomProgressDialog mProgressDialog;

	public ArrayList<String> mDistrictNames;
	public ArrayList<BankBranch> mBranches;
	public ArrayList<String> mBranchNames;

	private RecyclerView mAllCardListRecyclerView;
	private String cardType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_card);
		mProgressDialog = new CustomProgressDialog(this);
		mDistrictNames = new ArrayList<>();
		mBranches = new ArrayList<>();
		mBranchNames = new ArrayList<>();

		mFabAddNewBank = findViewById(R.id.fab_add_new_bank);
		mDescriptionTextView = findViewById(R.id.header_text_view);
		mAllCardListRecyclerView = findViewById(R.id.card_list);
		setTitle(this.getClass().getSimpleName());
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		setTitle(R.string.added_cards);
		getAddedCards();
		mFabAddNewBank.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess(ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD)
			public void onClick(View v) {
				showCardType();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			getAddedCards();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}

	private void getAddedCards() {
		if (mGetAllAddedCards == null) {
			mGetAllAddedCards = new HttpRequestGetAsyncTask(Constants.COMMAND_ADD_CARD,
					Constants.BASE_URL_MM + Constants.URL_GET_CARD, this, this, false);
			mGetAllAddedCards.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			mProgressDialog.show();
		}
	}

	private int getAppropriateCardIcon(String imageUrl) {
		return getResources().getIdentifier(imageUrl, "drawable", this.getPackageName());

	}

	@Override
	protected Context setContext() {
		return AddCardActivity.this;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utilities.hideKeyboard(this);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		mProgressDialog.dismiss();
		if (HttpErrorHandler.isErrorFound(result, this, null)) {
			mGetAllAddedCards = null;
		} else {
			try {
				Gson gson = new Gson();
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					AddCardResponse addCardResponse = gson.fromJson(result.getJsonString(), AddCardResponse.class);
					mCardList = addCardResponse.getUserCardList();
					if (mCardList.size() == 0) mDescriptionTextView.setVisibility(View.VISIBLE);
					else mDescriptionTextView.setVisibility(View.GONE);
					CardAdapter cardAdapter = new CardAdapter();
					mAllCardListRecyclerView.setAdapter(cardAdapter);
					mAllCardListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
					cardAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mGetAllAddedCards = null;
		}

	}

	public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

		@NonNull
		@Override
		public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_added_cards, parent, false);
			return new CardViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
			holder.mTitleTextView.setText(mCardList.get(position).getCardInfo());
			final int iconId = getAppropriateCardIcon(mCardList.get(position).getCardType().toLowerCase());
			if (iconId == INVALID_RESOURCE_ID) {
				holder.mCardImageView.setImageResource(R.drawable.basic_card);
			} else {
				holder.mCardImageView.setImageResource(iconId);
			}
			if (mCardList.get(position).getCardStatus().equals(Constants.VERIFIED)) {
				holder.mVerifyImageView.setVisibility(View.VISIBLE);
			} else {
				holder.mVerifyImageView.setVisibility(View.GONE);
			}
		}

		@Override
		public int getItemCount() {
			if (mCardList != null) {
				return mCardList.size();
			} else {
				return 0;
			}
		}

		 class CardViewHolder extends RecyclerView.ViewHolder {
			private final TextView mTitleTextView;
			private final ImageView mVerifyImageView;
			private final ImageView mCardImageView;

			 CardViewHolder(View itemView) {
				super(itemView);
				mTitleTextView = itemView.findViewById(R.id.title_text_view);
				mVerifyImageView = itemView.findViewById(R.id.verify_icon);
				mCardImageView = itemView.findViewById(R.id.icon_card);
			}
		}
	}

	private void showCardType() {
		final CardSelectDialog cardSelectDialog = new CardSelectDialog(AddCardActivity.this);
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

					Intent intent = new Intent(AddCardActivity.this, IPayTransactionActionActivity.class);
					intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD);
					intent.putExtra(Constants.CARD_TYPE, cardType);
					startActivity(intent);
				}else{
					Toaster.makeText(AddCardActivity.this, getString(R.string.select_card_type), Toast.LENGTH_LONG);
				}
			}
		});
		cardSelectDialog.show();
	}
}