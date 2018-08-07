package bd.com.ipay.ipayskeleton.HomeFragments.ContactsFragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.DatabaseHelper.SQLiteCursorLoader;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class BusinessContactsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener {

    private static final int CONTACTS_QUERY_LOADER = 0;

    private RecyclerView mRecyclerView;

    private SearchView mSearchView;

    private TextView mEmptyContactsTextView;

    private String mQuery = "";

    private ContactListAdapter mAdapter;
    private Cursor mCursor;

    private int businessNameIndex;
    private int phoneNumberIndex;
    private int profilePictureUrlIndex;
    private int businessTypeIndex;
    private int businessAddressIndex;
    private int businessThanaIndex;
    private int businessDistrictIndex;
    private int businessOutletIndex;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resetSearchKeyword();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        // We are using the SearchView at the bottom.
        mSearchView = (SearchView) v.findViewById(R.id.search_contacts);
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(this);

        // prevent auto focus on Dialog launch
        mSearchView.clearFocus();

        getLoaderManager().initLoader(CONTACTS_QUERY_LOADER, null, this).forceLoad();

        mEmptyContactsTextView = (TextView) v.findViewById(R.id.contact_list_empty);
        final RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = (RecyclerView) v.findViewById(R.id.contact_list);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new ContactListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_business_contact));
    }

    private void resetSearchKeyword() {
        if (mSearchView != null && !mQuery.isEmpty()) {
            Logger.logD("Loader", "Resetting.. Previous query: " + mQuery);

            mQuery = "";
            mSearchView.setQuery("", false);
            getLoaderManager().restartLoader(CONTACTS_QUERY_LOADER, null, this);
        }
    }

    @Override
    public void onDestroyView() {
        getLoaderManager().destroyLoader(CONTACTS_QUERY_LOADER);
        mCursor = null;
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new SQLiteCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                DataHelper dataHelper = DataHelper.getInstance(getActivity());

                Cursor cursor = dataHelper.searchBusinessAccounts(mQuery);

                if (cursor != null) {
                    businessNameIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_NAME);
                    phoneNumberIndex = cursor.getColumnIndex(DBConstants.KEY_MOBILE_NUMBER);
                    profilePictureUrlIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_PROFILE_PICTURE);
                    businessTypeIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_TYPE);
                    businessAddressIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_ADDRESS);
                    businessThanaIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_THANA);
                    businessDistrictIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_DISTRICT);
                    businessOutletIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_OUTLET);

                    this.registerContentObserver(cursor, DBConstants.DB_TABLE_BUSINESS_URI);
                }

                return cursor;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        populateList(data, getString(R.string.no_contacts));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
            mQuery = newText;
            getLoaderManager().restartLoader(CONTACTS_QUERY_LOADER, null, this);
        } else if (!newText.isEmpty() && newText.length() > 2) {
            mQuery = newText;
            getLoaderManager().restartLoader(CONTACTS_QUERY_LOADER, null, this);
        }

        return true;
    }

    private void populateList(Cursor cursor, String emptyText) {
        this.mCursor = cursor;

        if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
            mAdapter = new ContactListAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mEmptyContactsTextView.setVisibility(View.GONE);
        } else {
            mEmptyContactsTextView.setText(emptyText);
            mEmptyContactsTextView.setVisibility(View.VISIBLE);
        }
    }

    private class ContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int EMPTY_VIEW = 10;
        private static final int CONTACT_VIEW = 100;

        @SuppressWarnings("UnnecessaryLocalVariable")
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;

            if (viewType == EMPTY_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_description, parent, false);
                return new EmptyViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_business_contact, parent, false);
                return new ViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
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
            if (mCursor == null || mCursor.isClosed())
                return 0;
            else
                return mCursor.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            if (getItemCount() == 0)
                return EMPTY_VIEW;
            else
                return CONTACT_VIEW;
        }

        class EmptyViewHolder extends RecyclerView.ViewHolder {
            final TextView mEmptyDescription;

            EmptyViewHolder(View itemView) {
                super(itemView);
                mEmptyDescription = (TextView) itemView.findViewById(R.id.empty_description);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final View itemView;

            private final TextView businessNameView;
            private final TextView businessTypeView;
            private final ProfileImageView profilePictureView;
            private final TextView businessAddressView;

            public ViewHolder(View itemView) {
                super(itemView);

                this.itemView = itemView;

                businessNameView = (TextView) itemView.findViewById(R.id.business_name);
                businessTypeView = (TextView) itemView.findViewById(R.id.business_type);
                businessAddressView = (TextView) itemView.findViewById(R.id.business_address);
                profilePictureView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
            }

            public void bindView(int pos) {

                mCursor.moveToPosition(pos);
                final String businessName = mCursor.getString(businessNameIndex);
                final String mobileNumber = mCursor.getString(phoneNumberIndex);
                final int businessTypeID = mCursor.getInt(businessTypeIndex);
                final String businessPictureUrl = mCursor.getString(profilePictureUrlIndex);
                final String profilePictureUrl = Constants.BASE_URL_FTP_SERVER + mCursor.getString(profilePictureUrlIndex);
                final String businessAddress = mCursor.getString(businessAddressIndex);
                final String businessThana = mCursor.getString(businessThanaIndex);
                final String businessDistrict = mCursor.getString(businessDistrictIndex);
                final String businessOutlet = mCursor.getString(businessOutletIndex);

                if (businessName != null && !businessName.isEmpty()) {
                    businessNameView.setText(businessName);
                }

                if (businessAddress != null && !businessAddress.isEmpty()) {
                    businessAddressView.setText(businessAddress);
                }
                profilePictureView.setProfilePicture(profilePictureUrl, false);

                if (CommonData.getBusinessTypes() != null) {
                    BusinessType businessType = CommonData.getBusinessTypeById(businessTypeID);
                    if (businessType != null) {
                        businessTypeView.setText(businessType.getName());
                        businessTypeView.setVisibility(View.VISIBLE);
                    }
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        if (businessName != null && !businessName.isEmpty())
                            intent.putExtra(Constants.BUSINESS_NAME, businessName);
                        intent.putExtra(Constants.MOBILE_NUMBER, mobileNumber);
                        intent.putExtra(Constants.PROFILE_PICTURE, businessPictureUrl);
                        intent.putExtra(Constants.ADDRESS, businessAddress);
                        intent.putExtra(Constants.THANA, businessThana);
                        intent.putExtra(Constants.DISTRICT, businessDistrict);
                        intent.putExtra(Constants.OUTLET, businessOutlet);
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                });
            }

        }
    }

}
