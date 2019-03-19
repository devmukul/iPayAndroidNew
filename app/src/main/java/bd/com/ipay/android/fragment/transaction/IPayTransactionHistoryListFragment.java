package bd.com.ipay.android.fragment.transaction;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.math.BigDecimal;
import java.util.Locale;

import bd.com.ipay.android.adapter.TransactionHistoryAdapter;
import bd.com.ipay.android.adapter.viewholder.OnItemClickListener;
import bd.com.ipay.android.datasource.NetworkDataSource;
import bd.com.ipay.android.fragment.IPayProgressFragment;
import bd.com.ipay.android.utility.NetworkState;
import bd.com.ipay.android.utility.TransactionHistoryType;
import bd.com.ipay.android.viewmodel.RepositoryArguments;
import bd.com.ipay.android.viewmodel.TransactionHistoryRepositoryArguments;
import bd.com.ipay.android.viewmodel.TransactionHistoryRepositoryViewModel;
import bd.com.ipay.android.viewmodel.TransactionHistoryViewModel;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SentReceivedRequestPaymentReviewActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SentReceivedRequestReviewActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TransactionDetailsActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.ViewModel.ViewModelFactory;

public class IPayTransactionHistoryListFragment extends IPayProgressFragment {

	private static final int REQUEST_MONEY_REVIEW_REQUEST = 101;
	private static final int REQUEST_PAYMENT_REVIEW_REQUEST = 102;

	private TransactionHistoryViewModel transactionHistoryViewModel;
	private TransactionHistoryRepositoryViewModel transactionHistoryRepositoryViewModel;
	private TransactionHistoryType transactionHistoryType;

	private TransactionHistoryAdapter transactionHistoryAdapter;

	private final Handler handler = new Handler();

	private LinearLayoutManager linearLayoutManager;
	private Button toListTopButton;
	private SwipeRefreshLayout swipeRefreshLayout;

	private final Runnable isTopListItemVisibleCheckRunnable = new Runnable() {
		@Override
		public void run() {
			if (isAdded()) {
				if (linearLayoutManager != null) {
					if (linearLayoutManager.findFirstVisibleItemPosition() > 0) {
						toListTopButton.performClick();
					}
				}
			}
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Application application = MyApplication.getMyApplicationInstance();

		if (getArguments() != null) {
			transactionHistoryType = (TransactionHistoryType) getArguments()
					.getSerializable(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_TYPE_KEY);
		}

		if (transactionHistoryType == null) {
			transactionHistoryType = TransactionHistoryType.COMPLETED;
		}

		if (getActivity() != null) {
			transactionHistoryRepositoryViewModel = ViewModelProviders.of(getActivity()
					, ViewModelFactory.getTransactionHistoryViewModelFactory(
							transactionHistoryType, application))
					.get(getTransactionHistoryRepositoryViewModelKey(transactionHistoryType),
							TransactionHistoryRepositoryViewModel.class);

			transactionHistoryViewModel = ViewModelProviders.of(getActivity())
					.get(getTransactionHistoryViewModelKey(transactionHistoryType),
							TransactionHistoryViewModel.class);
		} else {
			transactionHistoryRepositoryViewModel = ViewModelProviders.of(this
					, ViewModelFactory.getTransactionHistoryViewModelFactory(
							transactionHistoryType, application))
					.get(getTransactionHistoryRepositoryViewModelKey(transactionHistoryType),
							TransactionHistoryRepositoryViewModel.class);

			if (getParentFragment() != null) {
				transactionHistoryViewModel = ViewModelProviders.of(getParentFragment())
						.get(getTransactionHistoryViewModelKey(transactionHistoryType),
								TransactionHistoryViewModel.class);
			} else if (getFragmentManager() != null) {
				getFragmentManager().beginTransaction().remove(this).commit();
			}
		}
		transactionHistoryAdapter = new TransactionHistoryAdapter(onRetryListener,
				onItemClickListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!transactionHistoryRepositoryViewModel.getPagedData().hasActiveObservers()) {
			transactionHistoryRepositoryViewModel.getPagedData().removeObservers(this);
			transactionHistoryRepositoryViewModel.getPagedData().observe(this,
					transactionHistoryListObserver);
		}
		if (!transactionHistoryRepositoryViewModel.getRefreshState().hasActiveObservers()) {
			transactionHistoryRepositoryViewModel.getRefreshState().removeObservers(this);
			transactionHistoryRepositoryViewModel.getRefreshState().observe(this,
					transactionHistoryRefreshObserver);
		}
		if (!transactionHistoryRepositoryViewModel.getNetworkState().hasActiveObservers()) {
			transactionHistoryRepositoryViewModel.getNetworkState().removeObservers(this);
			transactionHistoryRepositoryViewModel.getNetworkState().observe(this,
					transactionNetworkStateObserver);
		}
		if (!transactionHistoryViewModel.getTransactionHistorySearchLiveData().hasActiveObservers()) {
			transactionHistoryViewModel.getTransactionHistorySearchLiveData().removeObservers(this);
			transactionHistoryViewModel.getTransactionHistorySearchLiveData().observe(this,
					transactionHistorySearchLiveData);
		}
	}

	@NonNull
	@Override
	public View onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                                @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_transaction_history_list,
				container, false);
	}

	private String getTransactionHistoryViewModelKey(
			TransactionHistoryType transactionHistoryType) {
		return String.format(Locale.getDefault(), "%s:%s", transactionHistoryType.toString(),
				TransactionHistoryViewModel.class.getCanonicalName());
	}

	private String getTransactionHistoryRepositoryViewModelKey(
			TransactionHistoryType transactionHistoryType) {
		return String.format(Locale.getDefault(), "%s:%s", transactionHistoryType.toString(),
				TransactionHistoryRepositoryViewModel.class.getCanonicalName());
	}

	private final OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(int position, View view) {
			final TransactionHistory transactionHistory =
					transactionHistoryRepositoryViewModel.getItem(position);
			if (transactionHistory != null) {
				final int serviceId = transactionHistory.getServiceId();
				if (serviceId == Constants.TRANSACTION_HISTORY_REQUEST_MONEY && transactionHistoryType ==TransactionHistoryType.PENDING) {
					launchRequestMoneyReviewPage(transactionHistory);
				} else if (serviceId == Constants.TRANSACTION_HISTORY_REQUEST_PAYMENT && transactionHistoryType ==TransactionHistoryType.PENDING) {
					launchRequestPaymentReviewPage(transactionHistory);
				} else {
					launchTransactionHistoryPage(transactionHistory);
				}
			}
		}
	};

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setProgressText(R.string.please_wait);
		swipeRefreshLayout = view.findViewById(R.id.transaction_history_refresh_layout);
		final RecyclerView transactionHistoryRecyclerView
				= view.findViewById(R.id.transaction_history_recycler_view);
		toListTopButton = view.findViewById(R.id.to_list_top_button);

		toListTopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toListTopButton.setVisibility(View.GONE);
				linearLayoutManager.smoothScrollToPosition(transactionHistoryRecyclerView,
						new RecyclerView.State(), 0);
			}
		});

		linearLayoutManager = (LinearLayoutManager) transactionHistoryRecyclerView.getLayoutManager();
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				transactionHistoryRepositoryViewModel.refreshData();
			}
		});

		transactionHistoryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (toListTopButton.getVisibility() == View.VISIBLE) {
					if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
						toListTopButton.setVisibility(View.GONE);
					}
				}
			}
		});

		transactionHistoryRecyclerView.setAdapter(transactionHistoryAdapter);

		if (transactionHistoryRepositoryViewModel.getPagedData().getValue() != null &&
				!transactionHistoryRepositoryViewModel.getPagedData().getValue().isEmpty()) {
			transactionHistoryAdapter.submitList(transactionHistoryRepositoryViewModel
					.getPagedData().getValue());
			if (transactionHistoryViewModel.isForceRefreshData()) {
				transactionHistoryRepositoryViewModel.refreshData();
				swipeRefreshLayout.setRefreshing(true);
				transactionHistoryViewModel.setForceRefreshData(false);
			}
			showContentView();
		} else {
			transactionHistoryRepositoryViewModel.fetchData(new TransactionHistoryRepositoryArguments(
					RepositoryArguments.DEFAULT_PAGE_SIZE,
					null, null, null, null));
			hideContentView();
		}
	}

	private void launchRequestMoneyReviewPage(TransactionHistory transactionHistory) {
		Intent intent = new Intent(getActivity(), SentReceivedRequestReviewActivity.class);
		intent.putExtra(Constants.AMOUNT, new BigDecimal(transactionHistory.getAmount()));
		intent.putExtra(Constants.RECEIVER_MOBILE_NUMBER,
				ContactEngine.formatMobileNumberBD(transactionHistory.getAdditionalInfo().getNumber()));

		intent.putExtra(Constants.DESCRIPTION_TAG, transactionHistory.getPurpose());
		intent.putExtra(Constants.TRANSACTION_ID, transactionHistory.getTransactionID());
		intent.putExtra(Constants.NAME, transactionHistory.getReceiver());
		intent.putExtra(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + transactionHistory.getAdditionalInfo().getUserProfilePic());
		intent.putExtra(Constants.SWITCHED_FROM_TRANSACTION_HISTORY, true);
		intent.putExtra(Constants.IS_IN_CONTACTS,
				new ContactSearchHelper(getActivity()).searchMobileNumber(transactionHistory.getAdditionalInfo().getNumber()));

		if (transactionHistory.getType().equalsIgnoreCase(Constants.TRANSACTION_TYPE_CREDIT)) {
			intent.putExtra(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_SENT_REQUEST);
		}
		startActivityForResult(intent, REQUEST_MONEY_REVIEW_REQUEST);
	}

	private void launchRequestPaymentReviewPage(TransactionHistory transactionHistory) {
		Intent intent = new Intent(getActivity(), SentReceivedRequestPaymentReviewActivity.class);
		intent.putExtra(Constants.AMOUNT, new BigDecimal(transactionHistory.getAmount()));
		intent.putExtra(Constants.RECEIVER_MOBILE_NUMBER,
				ContactEngine.formatMobileNumberBD(transactionHistory.getAdditionalInfo().getNumber()));

		intent.putExtra(Constants.DESCRIPTION_TAG, transactionHistory.getPurpose());
		intent.putExtra(Constants.TRANSACTION_ID, transactionHistory.getTransactionID());
		intent.putExtra(Constants.NAME, transactionHistory.getReceiver());
		intent.putExtra(Constants.STATUS, Constants.HTTP_RESPONSE_STATUS_PROCESSING);
		intent.putExtra(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + transactionHistory.getAdditionalInfo().getUserProfilePic());
		intent.putExtra(Constants.SWITCHED_FROM_TRANSACTION_HISTORY, true);
		intent.putExtra(Constants.IS_IN_CONTACTS,
				new ContactSearchHelper(getActivity()).searchMobileNumber(transactionHistory.getAdditionalInfo().getNumber()));

		if (transactionHistory.getType().equalsIgnoreCase(Constants.TRANSACTION_TYPE_CREDIT)) {
			intent.putExtra(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_SENT_REQUEST);
		}
		startActivityForResult(intent, REQUEST_PAYMENT_REVIEW_REQUEST);
	}

	private void launchTransactionHistoryPage(TransactionHistory transactionHistory) {
		Intent intent = new Intent(getActivity(), TransactionDetailsActivity.class);
		intent.putExtra(Constants.TRANSACTION_DETAILS, transactionHistory);
		startActivity(intent);
	}

	private final Observer<PagedList<TransactionHistory>> transactionHistoryListObserver =
			new Observer<PagedList<TransactionHistory>>() {
				@Override
				public void onChanged(@Nullable PagedList<TransactionHistory>
						                      transactionHistories) {
					swipeRefreshLayout.setRefreshing(false);
					transactionHistoryAdapter.submitList(transactionHistories);
					handler.postDelayed(isTopListItemVisibleCheckRunnable, 500);
					showContentView();
				}
			};

	private final NetworkDataSource.OnRetryListener onRetryListener
			= new NetworkDataSource.OnRetryListener() {

		@Override
		public void onRetry() {
			transactionHistoryRepositoryViewModel.retry();
		}
	};

	private final Observer<NetworkState> transactionHistoryRefreshObserver
			= new Observer<NetworkState>() {
		@Override
		public void onChanged(@Nullable NetworkState networkState) {
			if (NetworkState.LOADED.equals(networkState)) {
				swipeRefreshLayout.setRefreshing(false);
			} else if (swipeRefreshLayout.isRefreshing()) {
				swipeRefreshLayout.setRefreshing(
						NetworkState.LOADING.equals(networkState) ||
								NetworkState.REFRESHING.equals(networkState));
			}
		}
	};

	private final Observer<NetworkState> transactionNetworkStateObserver
			= new Observer<NetworkState>() {
		@Override
		public void onChanged(@Nullable NetworkState networkState) {
			transactionHistoryAdapter.setNetworkSate(networkState);
		}
	};

	private final Observer<TransactionHistoryRepositoryArguments> transactionHistorySearchLiveData
			= new Observer<TransactionHistoryRepositoryArguments>() {
		@Override
		public void onChanged(@Nullable TransactionHistoryRepositoryArguments arguments) {
			if (arguments != null && !transactionHistoryRepositoryViewModel
					.isSameRepositoryArguments(arguments)) {
				swipeRefreshLayout.setRefreshing(true);
				transactionHistoryRepositoryViewModel.fetchData(arguments);
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
			case Activity.RESULT_OK:
				transactionHistoryRepositoryViewModel.refreshData();
				swipeRefreshLayout.setRefreshing(true);
				if (transactionHistoryType == TransactionHistoryType.PENDING) {
					if (getContext() != null) {
						Intent broadcastIntent = new Intent();
						Intent intent = new Intent();
						intent.setAction(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_UPDATE_ACTION);
						intent.putExtra(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_TYPE_KEY,
								TransactionHistoryType.COMPLETED);
						LocalBroadcastManager.getInstance(getContext())
								.sendBroadcast(broadcastIntent);
					}
				}
				break;
		}
	}
}
