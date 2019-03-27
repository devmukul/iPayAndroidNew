package bd.com.ipay.ipayskeleton.ContactFragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.DatabaseHelper.SQLiteCursorLoader;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments.IPayRequestMoneyAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SendMoneyFragments.IPaySendMoneyAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class IPayContactListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
		SearchView.OnQueryTextListener {

	private static final int CONTACTS_QUERY_LOADER = 0;

	private RecyclerView mContactListRecyclerView;
	private Button mContinueButton;
	private SearchView mContactSearchView;
	private TextView mContactListEmptyMessageTextView;
	private TextView mSearchedNumberTextView;
	private TextView mActionNameTextView;
	private String mQuery = "";


	private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
	private CustomProgressDialog mProgressDialog;

	private ContactListAdapter mAdapter;
	private Cursor mCursor;

	private int nameIndex;
	private int originalNameIndex;
	private int phoneNumberIndex;
	private int profilePictureUrlQualityMediumIndex;
	private LinearLayout mSearchedNumberLayout;

	private int transactionType;
    private static final String NUMERIC_PATTERN_WITH_SPACE = "^[0-9\\s\\+\\-!@#$%^&*(),.?\":{}|<>]*$";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProgressDialog = new CustomProgressDialog(getActivity());

		if (getArguments() != null) {
			transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_contact_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mActionNameTextView = view.findViewById(R.id.action_name_text_view);
		mSearchedNumberTextView = view.findViewById(R.id.searched_number_text_view);
		mContactSearchView = view.findViewById(R.id.contact_search_view);
		mSearchedNumberLayout = view.findViewById(R.id.searched_number_layout);
		mContactListEmptyMessageTextView = view.findViewById(R.id.contact_list_empty_message_text_view);
		mContactListRecyclerView = view.findViewById(R.id.contact_list_recycler_view);
		mContinueButton = view.findViewById(R.id.button_send_money);

		mContactSearchView.setIconified(false);
		mContactSearchView.setOnQueryTextListener(this);
		mContactSearchView.clearFocus();

		mAdapter = new ContactListAdapter();
		mContactListRecyclerView.setAdapter(mAdapter);
		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getProfileInfo(mQuery);
			}
		});
		getLoaderManager().initLoader(CONTACTS_QUERY_LOADER, null, IPayContactListFragment.this).forceLoad();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		resetSearchKeyword();
	}


	private void getProfileInfo(String mobileNumber) {
		if (mGetProfileInfoTask != null) {
			return;
		}
		final String formattedNumber = ContactEngine.formatMobileNumberBD(mobileNumber);
		GetUserInfoRequestBuilder mGetUserInfoRequestBuilder = new GetUserInfoRequestBuilder(ContactEngine.formatMobileNumberBD(mobileNumber));

		String mUri = mGetUserInfoRequestBuilder.getGeneratedUri();
		mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_USER_INFO,
				mUri, getContext(), new HttpResponseListener() {
			@Override
			public void httpResponseReceiver(GenericHttpResponse result) {
				if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
					mProgressDialog.dismiss();
					mGetProfileInfoTask = null;
				} else {
					try {
						if (result.getApiCommand().equals(Constants.COMMAND_GET_USER_INFO)) {
							mGetProfileInfoTask = null;
							mProgressDialog.dismiss();
							if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
								GetUserInfoResponse getUserInfoResponse = new Gson().fromJson(result.getJsonString(), GetUserInfoResponse.class);
								final String name = getUserInfoResponse.getName();
								final String profilePicture;
								if (getUserInfoResponse.getProfilePictures() != null && !getUserInfoResponse.getProfilePictures().isEmpty()) {
									profilePicture = Constants.BASE_URL_FTP_SERVER + getUserInfoResponse.getProfilePictures().get(0).getUrl();
								} else {
									profilePicture = "";
								}
								showAmountInput(name, profilePicture, formattedNumber);
							} else {
								Toast.makeText(getContext(), getString(R.string.user_has_no_ipay_account), Toast.LENGTH_LONG).show();
							}
						}
					} catch (Exception e) {
						mProgressDialog.dismiss();
						mGetProfileInfoTask = null;
					}
				}
			}
		}, false);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void resetSearchKeyword() {
		if (mContactSearchView != null && !mQuery.isEmpty()) {
			Logger.logD("Loader", "Resetting.. Previous query: " + mQuery);

			mQuery = "";
			mContactSearchView.setQuery("", false);
			getLoaderManager().restartLoader(CONTACTS_QUERY_LOADER, null, this);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getLoaderManager().destroyLoader(CONTACTS_QUERY_LOADER);
		if (mCursor != null && !mCursor.isClosed())
			mCursor.close();
	}

	@SuppressLint("StaticFieldLeak")
	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		return new SQLiteCursorLoader(getActivity()) {
			@Override
			public Cursor loadInBackground() {
				DataHelper dataHelper = DataHelper.getInstance(getActivity());

				// TODO hack
				/*
				 * Caution: It takes some time to load invite response from the server. So if you are
				 * loading this Fragment from Contacts page, it is very much possible that invitee list
				 * will be null. This is generally not a problem because invitee list is not used
				 * in Contacts fragment when doing database query. It is used in the database query
				 * from invite fragment, but by that time the invitee list should already have loaded.
				 */
				Cursor cursor = dataHelper.searchContacts(mQuery, true, false, false,
						false, false, false, null);

				if (cursor != null) {
					nameIndex = cursor.getColumnIndex(DBConstants.KEY_NAME);
					originalNameIndex = cursor.getColumnIndex(DBConstants.KEY_ORIGINAL_NAME);
					phoneNumberIndex = cursor.getColumnIndex(DBConstants.KEY_MOBILE_NUMBER);
					profilePictureUrlQualityMediumIndex = cursor.getColumnIndex(DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM);
					this.registerContentObserver(cursor, DBConstants.DB_TABLE_CONTACTS_URI);
				}
				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
		if (data != null && data.getCount() <= 0) {
			if (InputValidator.isValidNumber(mQuery)) {
				if (ContactEngine.formatMobileNumberBD(mQuery).equals(ProfileInfoCacheManager.getMobileNumber())) {
					return;
				}
				final String mActionName;
				switch (transactionType) {
					case IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY:
						mActionName = getString(R.string.send_money);
						break;
					case IPayTransactionActionActivity.TRANSACTION_TYPE_REQUEST_MONEY:
						mActionName = getString(R.string.request_money);
						break;
					case IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID:
					default:
						return;
				}
				mSearchedNumberLayout.setVisibility(View.VISIBLE);
				mContactListRecyclerView.setVisibility(View.GONE);
				mSearchedNumberTextView.setVisibility(View.VISIBLE);
				mContinueButton.setVisibility(View.VISIBLE);
				mActionNameTextView.setText(String.format("%s to", mActionName));
				mSearchedNumberTextView.setText(mQuery);
			} else {
				if (!mQuery.matches("\\d+")) {
					mActionNameTextView.setText(R.string.no_contacts);
				} else {
					mActionNameTextView.setText(R.string.error_invalid_mobile_number);
				}
				mSearchedNumberLayout.setVisibility(View.VISIBLE);
				mContactListRecyclerView.setVisibility(View.GONE);
				mSearchedNumberTextView.setVisibility(View.GONE);
				mContinueButton.setVisibility(View.GONE);
				mSearchedNumberTextView.setText(R.string.empty_string);
			}
		} else {
			mSearchedNumberLayout.setVisibility(View.GONE);
			mContactListRecyclerView.setVisibility(View.VISIBLE);
			mSearchedNumberTextView.setText(R.string.empty_string);
			populateList(data, getString(R.string.no_contacts));
		}
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {

	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		mQuery = newText;
		if(newText.matches(NUMERIC_PATTERN_WITH_SPACE)){
            newText = newText.replaceAll("[^0-9.]", "");
            mQuery = newText;
        }
		getLoaderManager().restartLoader(CONTACTS_QUERY_LOADER, null, this);
		return true;
	}

	private void populateList(Cursor cursor, String emptyText) {
		this.mCursor = cursor;

		if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
			mAdapter = new ContactListAdapter();
			mContactListRecyclerView.setAdapter(mAdapter);
			mContactListEmptyMessageTextView.setVisibility(View.GONE);
		} else {
			mContactListEmptyMessageTextView.setText(emptyText);
			mContactListEmptyMessageTextView.setVisibility(View.VISIBLE);
		}
	}

	public class ContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private final LayoutInflater layoutInflater;

		ContactListAdapter() {
			layoutInflater = LayoutInflater.from(getContext());
		}

		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			switch (viewType) {
				case R.layout.list_empty_description:
					return new EmptyViewHolder(layoutInflater.inflate(R.layout.list_empty_description, parent, false));
				case R.layout.list_item_contact:
					return new ViewHolder(layoutInflater.inflate(R.layout.list_item_contact, parent, false));
				default:
					return new RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.list_item_contact, parent, false)) {
					};
			}
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
			try {

				if (holder instanceof ViewHolder) {
					ViewHolder vh = (ViewHolder) holder;
					vh.bindView(position);
				} else if (holder instanceof EmptyViewHolder) {
					EmptyViewHolder vh = (EmptyViewHolder) holder;
					vh.mEmptyDescription.setText(getString(R.string.no_contacts));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getItemCount() {
			if (mCursor == null || mCursor.isClosed()) {
				return 0;
			} else {
				return mCursor.getCount();
			}
		}

		@Override
		public int getItemViewType(int position) {
			if (getItemCount() == 0)
				return R.layout.list_empty_description;
			else
				return R.layout.list_item_contact;
		}

		class EmptyViewHolder extends RecyclerView.ViewHolder {
			final TextView mEmptyDescription;

			EmptyViewHolder(View itemView) {
				super(itemView);
				mEmptyDescription = itemView.findViewById(R.id.empty_description);
			}
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private final View itemView;

			private final TextView name1View;
			private final TextView name2View;
			private final ProfileImageView profilePictureView;
			private final TextView mobileNumberView;

			public ViewHolder(View itemView) {
				super(itemView);

				this.itemView = itemView;

				name1View = itemView.findViewById(R.id.name1);
				name2View = itemView.findViewById(R.id.name2);
				mobileNumberView = itemView.findViewById(R.id.mobile_number);
				profilePictureView = itemView.findViewById(R.id.profile_picture);
			}

			public void bindView(final int pos) {

				mCursor.moveToPosition(pos);

				final String name = mCursor.getString(nameIndex);
				final String originalName = mCursor.getString(originalNameIndex);
				final String mobileNumber = mCursor.getString(phoneNumberIndex);
				final String profilePictureUrlQualityMedium = Constants.BASE_URL_FTP_SERVER + mCursor.getString(profilePictureUrlQualityMediumIndex);

				/*
				 * We need to show original name on the top if exists
				 */
				if (originalName != null && !originalName.isEmpty()) {
					name1View.setText(originalName);
					name2View.setVisibility(View.VISIBLE);
					name2View.setText(name);
				} else {
					name1View.setText(name);
					name2View.setVisibility(View.GONE);
				}

				mobileNumberView.setText(mobileNumber);

				profilePictureView.setProfilePicture(profilePictureUrlQualityMedium, false);

				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (ContactEngine.formatMobileNumberBD(mobileNumber).equals(ProfileInfoCacheManager.getMobileNumber())) {
							switch (transactionType) {
								case IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY:
									Toaster.makeText(getContext(), R.string.you_cannot_send_money_to_your_number, Toast.LENGTH_SHORT);
									break;
								case IPayTransactionActionActivity.TRANSACTION_TYPE_REQUEST_MONEY:
									Toaster.makeText(getContext(), R.string.you_cannot_request_money_from_your_number, Toast.LENGTH_SHORT);
									break;
							}
							return;
						}
						showAmountInput(originalName, profilePictureUrlQualityMedium, mobileNumber);
					}
				});
			}
		}

	}

	private void showAmountInput(String name, String profilePictureUrlQualityMedium, String mobileNumber) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.NAME, name);
		bundle.putString(Constants.PHOTO_URI, profilePictureUrlQualityMedium);
		bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
		bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
		if (getActivity() instanceof IPayTransactionActionActivity) {
			if (transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY) {
				((IPayTransactionActionActivity) getActivity()).switchFragment(new IPaySendMoneyAmountInputFragment(), bundle, 1, true);
			} else if (transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_REQUEST_MONEY) {
				((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayRequestMoneyAmountInputFragment(), bundle, 1, true);
			} else {
				((IPayTransactionActionActivity) getActivity()).switchToAmountInputFragment(bundle);
			}
		}
	}
}
