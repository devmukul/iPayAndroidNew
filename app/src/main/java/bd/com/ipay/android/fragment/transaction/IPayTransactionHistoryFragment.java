package bd.com.ipay.android.fragment.transaction;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

import bd.com.ipay.android.utility.TransactionHistoryType;
import bd.com.ipay.android.viewmodel.TransactionHistoryViewModel;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayTransactionHistoryFragment extends BaseFragment {

	public static final String TRANSACTION_HISTORY_TYPE_KEY = "TRANSACTION_HISTORY_TYPE";
	public static final String HAS_TRANSACTION_SEARCH_KEY = "HAS_TRANSACTION_SEARCH";
	public static final String TRANSACTION_HISTORY_UPDATE_ACTION = "TRANSACTION_HISTORY_UPDATE_ACTION";
	private TransactionHistoryType transactionHistoryType;

	private TransactionHistoryViewModel transactionHistoryViewModel;
	private boolean isTransactionSearchable;

	private SearchView transactionSearchView;
	private TextView filterTitleTextView;
	private PopupMenu popupMenu;
	private ImageButton filterOptionPopupMenuButton;
	private ImageButton clearAllFilterButton;
	private Button removeFilterButton;

	private IPayTransactionHistoryServiceFilterFragment iPayTransactionHistoryServiceFilterFragment;
	private IPayTransactionHistoryDateFilterFragment iPayTransactionHistoryDateFilterFragment;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			transactionHistoryType = (TransactionHistoryType) getArguments()
					.getSerializable(TRANSACTION_HISTORY_TYPE_KEY);
			isTransactionSearchable = getArguments()
					.getBoolean(HAS_TRANSACTION_SEARCH_KEY, true);
		}

		if (transactionHistoryType == null) {
			transactionHistoryType = TransactionHistoryType.COMPLETED;
		}

		if (getActivity() != null) {
			transactionHistoryViewModel =
					ViewModelProviders.of(getActivity()).get(
							getTransactionHistoryViewModelKey(transactionHistoryType)
							, TransactionHistoryViewModel.class);

			LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
					transactionHistoryRefreshActionBroadcastReceiver,
					new IntentFilter(TRANSACTION_HISTORY_UPDATE_ACTION)
			);
		} else {
			transactionHistoryViewModel =
					ViewModelProviders.of(this).get(
							getTransactionHistoryViewModelKey(transactionHistoryType)
							, TransactionHistoryViewModel.class);
		}

		if (getActivity() != null) {
			mTracker = Utilities.getTracker(getActivity());
		}
	}

	@Override
	public void onDestroy() {
		if (getActivity() != null) {
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
					transactionHistoryRefreshActionBroadcastReceiver);
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!transactionHistoryViewModel.getAddedFilterLiveData().hasActiveObservers()) {
			transactionHistoryViewModel.getAddedFilterLiveData().removeObservers(this);
			transactionHistoryViewModel.getAddedFilterLiveData().observe(this, searchTypeObserver);
		}
	}

	private String getTransactionHistoryViewModelKey(
			TransactionHistoryType transactionHistoryType) {
		return String.format(Locale.getDefault(), "%s:%s", transactionHistoryType.toString(),
				TransactionHistoryViewModel.class.getCanonicalName());
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_transaction_history, container,
				false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initGlobalScopeViews(view);

		if (getFragmentManager() != null) {
			IPayTransactionHistoryListFragment iPayTransactionHistoryListFragment
					= new IPayTransactionHistoryListFragment();
			Bundle bundle = new Bundle();
			bundle.putSerializable(TRANSACTION_HISTORY_TYPE_KEY, transactionHistoryType);
			iPayTransactionHistoryListFragment.setArguments(bundle);
			getFragmentManager().beginTransaction().replace(
					R.id.fragment_container_transaction_history_list,
					iPayTransactionHistoryListFragment)
					.commit();
		}
	}

	private void initGlobalScopeViews(@NonNull final View view) {
		transactionSearchView = view.findViewById(R.id.transaction_search_view);
		filterOptionPopupMenuButton = view.findViewById(R.id.filter_option_popup_menu_button);
		clearAllFilterButton = view.findViewById(R.id.clear_all_filter_button);
		removeFilterButton = view.findViewById(R.id.remove_filter_button);
		filterTitleTextView = view.findViewById(R.id.filter_title_text_view);

		transactionSearchView.setVisibility(isTransactionSearchable ? View.VISIBLE : View.GONE);

		filterOptionPopupMenuButton.setOnClickListener(buttonActionListener);
		clearAllFilterButton.setOnClickListener(buttonActionListener);
		removeFilterButton.setOnClickListener(buttonActionListener);

		if (getActivity() != null) {
			popupMenu = new PopupMenu(getActivity(), filterOptionPopupMenuButton);
			popupMenu.getMenuInflater()
					.inflate(R.menu.activity_transaction_history, popupMenu.getMenu());
			popupMenu.setOnMenuItemClickListener(popUpMenuOnClickListener);
		}

		transactionSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				if (!TextUtils.isEmpty(s)) {
					setupFilterButtonsVisibility(View.GONE, View.GONE, View.GONE);
					transactionHistoryViewModel
							.filterTransactionHistory(TransactionHistoryViewModel.SearchType.KEY, s);
					return false;
				} else {
					return false;
				}
			}

			@Override
			public boolean onQueryTextChange(String s) {
				if (!TextUtils.isEmpty(s)) {
					setupFilterButtonsVisibility(View.GONE, View.GONE, View.GONE);
					return false;
				} else {
					setupFilterButtonsVisibility(View.VISIBLE, View.GONE, View.GONE);
					return false;
				}
			}
		});

		transactionSearchView.findViewById(R.id.search_close_btn)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						transactionSearchView.setQuery("", false);
						transactionSearchView.clearFocus();
						transactionHistoryViewModel.clearFilter();

					}
				});
	}

	private void setTransactionTitleTextView() {
		switch (transactionHistoryType) {
			case COMPLETED:
				filterTitleTextView.setText(R.string.completed_transaction_list);
				break;
			case PENDING:
				filterTitleTextView.setText(R.string.pending_transaction_list);
				break;
			default:
				filterTitleTextView.setText(R.string.empty_string);
				break;
		}
	}

	private void setupFilterViewsVisibility(int transactionSearchViewVisibility) {
		transactionSearchView.setVisibility(isTransactionSearchable ?
				transactionSearchViewVisibility : View.GONE);
	}

	private void setupFilterButtonsVisibility(int filterOptionPopupMenuButtonVisibility,
	                                          int clearAllFilterButtonVisibility,
	                                          int removeFilterButtonVisibility) {
		filterOptionPopupMenuButton.setVisibility(filterOptionPopupMenuButtonVisibility);
		clearAllFilterButton.setVisibility(clearAllFilterButtonVisibility);
		removeFilterButton.setVisibility(removeFilterButtonVisibility);
	}

	private final View.OnClickListener buttonActionListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.filter_option_popup_menu_button:
					if (popupMenu != null) {
						popupMenu.show();
					}
					break;
				case R.id.clear_all_filter_button:
				case R.id.remove_filter_button:
					setupFilterButtonsVisibility(View.VISIBLE, ViewGroup.GONE, View.GONE);
					setupFilterViewsVisibility(View.VISIBLE);
					setTransactionTitleTextView();
					transactionHistoryViewModel.clearFilter();
					break;
			}
		}
	};

	private final PopupMenu.OnMenuItemClickListener popUpMenuOnClickListener
			= new PopupMenu.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			switch (menuItem.getItemId()) {
				case R.id.action_filter_by_date:
					if (getFragmentManager() != null) {
						if (iPayTransactionHistoryDateFilterFragment == null) {
							iPayTransactionHistoryDateFilterFragment
									= new IPayTransactionHistoryDateFilterFragment();
							iPayTransactionHistoryDateFilterFragment.setArguments(getArguments());
						}
						iPayTransactionHistoryDateFilterFragment
								.show(getFragmentManager(),
										iPayTransactionHistoryDateFilterFragment.getTag());
					}

					return true;
				case R.id.action_filter_by_service:
					if (getFragmentManager() != null) {
						if (iPayTransactionHistoryServiceFilterFragment == null) {
							iPayTransactionHistoryServiceFilterFragment
									= new IPayTransactionHistoryServiceFilterFragment();
							iPayTransactionHistoryServiceFilterFragment.setArguments(getArguments());
						}
						iPayTransactionHistoryServiceFilterFragment
								.show(getFragmentManager(),
										iPayTransactionHistoryServiceFilterFragment.getTag());
					}
					return true;
				default:
					return false;
			}
		}
	};

	private final Observer<TransactionHistoryViewModel.SearchType> searchTypeObserver =
			new Observer<TransactionHistoryViewModel.SearchType>() {
				@Override
				public void onChanged(@Nullable TransactionHistoryViewModel.SearchType searchType) {
					if (searchType != null)
						updateSearchBarView(searchType);
				}
			};

	private void updateSearchBarView(@NonNull TransactionHistoryViewModel.SearchType searchType) {
		switch (searchType) {
			case ALL:
				setupFilterButtonsVisibility(View.VISIBLE, View.GONE, View.GONE);
				setupFilterViewsVisibility(View.VISIBLE);

				break;
			case SERVICE:
				setupFilterButtonsVisibility(View.GONE, View.GONE, View.VISIBLE);
				setupFilterViewsVisibility(View.GONE);
				filterTitleTextView.setText(R.string.filter_by_service);
				break;
			case DATE:
				setupFilterButtonsVisibility(View.GONE, View.GONE, View.VISIBLE);
				setupFilterViewsVisibility(View.GONE);
				filterTitleTextView.setText(R.string.filter_by_date);
				break;
			case KEY:
				final String filterKey;
				if (transactionHistoryViewModel.getTransactionHistorySearchLiveData().getValue() != null) {
					filterKey = transactionHistoryViewModel
							.getTransactionHistorySearchLiveData().getValue().getSearchText();
				} else {
					filterKey = null;
				}

				if (!TextUtils.isEmpty(filterKey) && TextUtils.isEmpty(transactionSearchView.getQuery())) {
					setupFilterButtonsVisibility(View.GONE, View.GONE, View.GONE);
					transactionSearchView.setQuery(filterKey, false);
				}
				setupFilterViewsVisibility(View.VISIBLE);
				break;

		}
	}

	private final BroadcastReceiver transactionHistoryRefreshActionBroadcastReceiver =
			new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (TRANSACTION_HISTORY_UPDATE_ACTION.equals(intent.getAction())) {
						transactionHistoryViewModel.setForceRefreshData(true);
					}
				}
			};
}

