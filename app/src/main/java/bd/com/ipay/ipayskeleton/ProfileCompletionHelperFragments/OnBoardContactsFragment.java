package bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Api.ContactApi.DeleteContactAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.DatabaseHelper.SQLiteCursorLoader;
import bd.com.ipay.ipayskeleton.HomeFragments.ContactsFragments.ContactsHolderFragment;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.AskForIntroductionResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.SendInviteResponse;
import bd.com.ipay.ipayskeleton.Model.Contact.DeleteContactRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.Contact.InviteContactNode;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

/**
 * CAUTION: This fragment is used in contacts tab, invite page, and in contact picker.
 * Make sure to test it thoroughly after making any changes.
 * <p/>
 * <p/>
 * Pass (Constants.VERIFIED_USERS_ONLY, true) in the argument bundle to show only the
 * verified iPay users and (Constants.IPAY_MEMBERS_ONLY, true) to show member users only.
 */
public class OnBoardContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener,
        HttpResponseListener {

    private static final String TAG = OnBoardContactsFragment.class.getSimpleName();

    private static final int CONTACTS_QUERY_LOADER = 0;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private TextView mEmptyContactsTextView;

    private String mQuery = "";
    // When a contact item is clicked, we need to access its name and number from the sheet view.
    // So saving these in these two variables.
    private String mSelectedNumber;

    private HttpRequestPostAsyncTask mSendInviteTask = null;
    private HttpRequestPostAsyncTask mAskForRecommendationTask = null;
    private CustomProgressDialog mProgressDialog;

    private ContactListAdapter mAdapter;
    private Cursor mCursor;
    private int nameIndex;
    private int originalNameIndex;
    private int phoneNumberIndex;
    private int profilePictureUrlQualityMediumIndex;
    private int profilePictureUrlQualityHighIndex;
    private int verificationStatusIndex;
    private int isMemberIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resetSearchKeyword();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboard_contacts, container, false);
        mProgressDialog = new CustomProgressDialog(getActivity());

        // If the fragment is a dialog fragment, we are using the searchview at the bottom.
        // Otherwise, we are using the searchview from the action bar.
        mSearchView = view.findViewById(R.id.search_contacts);
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(this);

        // prevent auto focus on Dialog launch
        mSearchView.clearFocus();

        getLoaderManager().initLoader(CONTACTS_QUERY_LOADER, null, this).forceLoad();

        mEmptyContactsTextView = view.findViewById(R.id.contact_list_empty_message_text_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = view.findViewById(R.id.contact_list);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ContactListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                List<String> invitees = null;
                if (ContactsHolderFragment.mGetInviteInfoResponse != null)
                    invitees = ContactsHolderFragment.mGetInviteInfoResponse.getInvitees();

                Cursor cursor = dataHelper.searchContacts(mQuery, false, false, false,
                        false, false, false, invitees);

                if (cursor != null) {
                    nameIndex = cursor.getColumnIndex(DBConstants.KEY_NAME);
                    originalNameIndex = cursor.getColumnIndex(DBConstants.KEY_ORIGINAL_NAME);
                    phoneNumberIndex = cursor.getColumnIndex(DBConstants.KEY_MOBILE_NUMBER);
                    profilePictureUrlQualityMediumIndex = cursor.getColumnIndex(DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM);
                    profilePictureUrlQualityHighIndex = cursor.getColumnIndex(DBConstants.KEY_PROFILE_PICTURE_QUALITY_HIGH);
                    verificationStatusIndex = cursor.getColumnIndex(DBConstants.KEY_VERIFICATION_STATUS);
                    isMemberIndex = cursor.getColumnIndex(DBConstants.KEY_IS_MEMBER);
                    this.registerContentObserver(cursor, DBConstants.DB_TABLE_CONTACTS_URI);
                }

                return cursor;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        populateList(data, getString(R.string.no_contacts));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    boolean isDialogFragment() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mQuery = newText;
        getLoaderManager().restartLoader(CONTACTS_QUERY_LOADER, null, this);

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

    private void showDeleteContactConfirmationDialog(final String mobileNumber) {
        if (getActivity() != null) {
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.remove_contact_title)
                    .cancelable(false)
                    .content(R.string.remove_contact_message)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .show();

            dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    deleteContact(mobileNumber);
                }
            });

            dialog.show();
        }
    }

    private void deleteContact(String phoneNumber) {
        DeleteContactRequestBuilder deleteContactRequestBuilder = new DeleteContactRequestBuilder(phoneNumber);

        new DeleteContactAsyncTask(Constants.COMMAND_DELETE_CONTACTS,
                deleteContactRequestBuilder.generateUri(), deleteContactRequestBuilder.getDeleteContactRequest(),
                getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendRecommendationRequest(String mobileNumber) {
        if (mAskForRecommendationTask != null) {
            return;
        }
        mProgressDialog.show();
        mAskForRecommendationTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ASK_FOR_RECOMMENDATION,
                Constants.BASE_URL_MM + Constants.URL_ASK_FOR_INTRODUCTION + mobileNumber, null, getActivity(), false);
        mAskForRecommendationTask.mHttpResponseListener = this;
        mAskForRecommendationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendInvite(String phoneNumber, boolean wantToIntroduce) {
        try {
            if (ContactsHolderFragment.mGetInviteInfoResponse != null && ContactsHolderFragment.mGetInviteInfoResponse.invitees != null) {
                int numberOfInvitees = ContactsHolderFragment.mGetInviteInfoResponse.invitees.size();
                if (numberOfInvitees >= ContactsHolderFragment.mGetInviteInfoResponse.totalLimit) {
                    Toast.makeText(getActivity(), R.string.invitation_limit_exceeded,
                            Toast.LENGTH_LONG).show();
                } else {
                    mProgressDialog.show();

                    InviteContactNode inviteContactNode = new InviteContactNode(phoneNumber, wantToIntroduce);
                    Gson gson = new Gson();
                    String json = gson.toJson(inviteContactNode, InviteContactNode.class);
                    mSendInviteTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SEND_INVITE,
                            Constants.BASE_URL_MM + Constants.URL_SEND_INVITE, json, getActivity(), this, false);
                    mSendInviteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } catch (Exception e) {
            Logger.logD(TAG, e.getMessage());
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mSendInviteTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SEND_INVITE)) {
            try {
                SendInviteResponse mSendInviteResponse = gson.fromJson(result.getJsonString(), SendInviteResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.invitation_sent, Toast.LENGTH_LONG).show();
                    }

                    if (ContactsHolderFragment.mGetInviteInfoResponse != null)
                        ContactsHolderFragment.mGetInviteInfoResponse.invitees.add(mSelectedNumber);

                    getLoaderManager().restartLoader(CONTACTS_QUERY_LOADER, null, this);

                } else if (getActivity() != null) {
                    Toaster.makeText(getActivity(), mSendInviteResponse.getMessage(), Toast.LENGTH_LONG);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Toaster.makeText(getActivity(), R.string.failed_sending_invitation, Toast.LENGTH_LONG);
                }
            }

            mProgressDialog.dismiss();
            mSendInviteTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_ASK_FOR_RECOMMENDATION)) {
            try {
                AskForIntroductionResponse mAskForIntroductionResponse = gson.fromJson(result.getJsonString(), AskForIntroductionResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toaster.makeText(getActivity(), R.string.introduction_request_sent, Toast.LENGTH_LONG);
                    }
                } else if (getActivity() != null) {
                    Toaster.makeText(getActivity(), mAskForIntroductionResponse.getMessage(), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (getActivity() != null) {
                    Toaster.makeText(getActivity(), R.string.failed_asking_introduction, Toast.LENGTH_LONG);
                }
            }

            mProgressDialog.dismiss();
            mAskForRecommendationTask = null;
        }
    }

    public void showInviteDialog(String name, final String mobileNumber) {
        String mInviteMessage = getString(R.string.are_you_sure_to_invite);
        if (!name.isEmpty())
            mInviteMessage = mInviteMessage.replace(getString(R.string.this_person), name);

        if (getActivity() != null) {
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.invite_to_ipay)
                    .customView(R.layout.dialog_invite_contact_with_introduction, true)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .show();
            View view = dialog.getCustomView();
            if (view == null)
                return;
            final TextView mInviteText = view.findViewById(R.id.textviewInviteMessage);
            final CheckBox introduceCheckbox = view.findViewById(R.id.introduceCheckbox);

            mInviteText.setText(mInviteMessage);

            dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                @ValidateAccess(ServiceIdConstants.MANAGE_INVITATIONS)
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                    boolean wantToIntroduce = introduceCheckbox.isChecked();

                    sendInvite(mobileNumber, wantToIntroduce);
                    dialog.dismiss();
                }
            });

            dialog.getBuilder().onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void setSelectedNumber(String contactNumber) {
        this.mSelectedNumber = contactNumber;
    }

    public class ContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int EMPTY_VIEW = 10;
        private static final int CONTACT_VIEW = 100;

        @NonNull
        @SuppressWarnings("UnnecessaryLocalVariable")
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v;

            if (viewType == EMPTY_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_description, parent, false);
                return new EmptyViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contact, parent, false);
                return new ViewHolder(v);
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
                mEmptyDescription = itemView.findViewById(R.id.empty_description);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final View itemView;

            private final TextView name1View;
            private final TextView name2View;
            private final ProfileImageView profilePictureView;
            private final TextView mobileNumberView;
            private final ImageView verificationStatus;
            private final Button button_ask;

            public ViewHolder(View itemView) {
                super(itemView);

                this.itemView = itemView;

                name1View = itemView.findViewById(R.id.name1);
                name2View = itemView.findViewById(R.id.name2);
                mobileNumberView = itemView.findViewById(R.id.mobile_number);
                profilePictureView = itemView.findViewById(R.id.profile_picture);
                verificationStatus = itemView.findViewById(R.id.verification_status);
                button_ask = itemView.findViewById(R.id.button_ask);
            }

            public void bindView(int pos) {

                mCursor.moveToPosition(pos);

                final String name = mCursor.getString(nameIndex);
                final String originalName = mCursor.getString(originalNameIndex);
                final String mobileNumber = mCursor.getString(phoneNumberIndex);
                final String profilePictureUrlQualityMedium = Constants.BASE_URL_FTP_SERVER + mCursor.getString(profilePictureUrlQualityMediumIndex);
                final String profilePictureUrlQualityHigh = Constants.BASE_URL_FTP_SERVER + mCursor.getString(profilePictureUrlQualityHighIndex);
                final boolean isVerified = mCursor.getInt(verificationStatusIndex) == DBConstants.VERIFIED_USER;
                final boolean isMember = mCursor.getInt(isMemberIndex) == DBConstants.IPAY_MEMBER;
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

                if (!isDialogFragment()) {

                    if (isMember) {
                        if (!isVerified) {
                            button_ask.setVisibility(View.GONE);
                            verificationStatus.setVisibility(View.GONE);
                        } else {
                            button_ask.setVisibility(View.VISIBLE);
                            verificationStatus.setVisibility(View.VISIBLE);
                        }
                    }
                    button_ask.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendRecommendationRequest(mobileNumber);
                        }
                    });
                }

                profilePictureView.setProfilePicture(profilePictureUrlQualityMedium, false);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isDialogFragment()) {
                            Intent intent = new Intent();
                            if (originalName != null && !originalName.isEmpty())
                                intent.putExtra(Constants.NAME, originalName);
                            else intent.putExtra(Constants.NAME, name);
                            intent.putExtra(Constants.MOBILE_NUMBER, mobileNumber);
                            intent.putExtra(Constants.PROFILE_PICTURE, profilePictureUrlQualityHigh);
                            if (getActivity() != null) {
                                getActivity().setResult(Activity.RESULT_OK, intent);
                                getActivity().finish();
                            }

                        } else if (!isMember) {
                            setSelectedNumber(mobileNumber);

                            showInviteDialog(name, mobileNumber);
                        } else {
                            setSelectedNumber(mobileNumber);
                            if (getActivity() != null)
                                Utilities.hideKeyboard(getActivity());
                        }
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    @ValidateAccess(ServiceIdConstants.DELETE_CONTACTS)
                    public boolean onLongClick(View view) {
                        showDeleteContactConfirmationDialog(mobileNumber);
                        return false;
                    }
                });
            }
        }
    }
}