package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestDeleteAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.AddNewEmailRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.AddNewEmailResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.DeleteEmailResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.Email;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.EmailVerificationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.GetEmailResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.MakePrimaryEmailResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.MakePrimaryRequest;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Service.GCM.PushNotificationStatusHolder;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefConstants;
import bd.com.ipay.ipayskeleton.Service.FCM.PushNotificationStatusHolder;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class EmailFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetEmailsTask = null;
    private GetEmailResponse mGetEmailResponse;

    private HttpRequestPostAsyncTask mAddNewEmailTask = null;
    private AddNewEmailResponse mAddNewEmailResponse;

    private HttpRequestDeleteAsyncTask mDeleteEmailTask = null;
    private DeleteEmailResponse mDeleteEmailResponse;

    private HttpRequestPostAsyncTask mMakePrimaryEmailTask = null;
    private MakePrimaryEmailResponse makePrimaryEmailResponse;

    private HttpRequestPostAsyncTask mEmailVerificationTask = null;
    private EmailVerificationResponse mEmailVerificationResponse;

    private List<Email> mEmails;
    private EmailListAdapter mEmailListAdapter;

    private RecyclerView mEmailListRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FloatingActionButton mFabAddNewEmail;
    private ProgressDialog mProgressDialog;

    private TextView mPrimaryEmailView;
    private TextView mPrimaryEmailViewHeader;
    private TextView mOtherEmailViewHeader;
    private ImageView mPrimaryVerificationStatus;

    private TextView mEmptyListTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_email, container, false);
        mFabAddNewEmail = (FloatingActionButton) v.findViewById(R.id.fab_add_email);

        mFabAddNewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewEmailDialog();
            }
        });

        getActivity().setTitle(R.string.email);

        mPrimaryEmailView = (TextView) v.findViewById(R.id.textview_email);
        mPrimaryEmailViewHeader = (TextView) v.findViewById(R.id.primary_email_header);
        mOtherEmailViewHeader = (TextView) v.findViewById(R.id.other_email_header);
        mPrimaryVerificationStatus = (ImageView) v.findViewById(R.id.email_verification_status);
        mEmailListRecyclerView = (RecyclerView) v.findViewById(R.id.list_email);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);

        mProgressDialog = new ProgressDialog(getActivity());

        mEmailListAdapter = new EmailListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mEmailListRecyclerView.setLayoutManager(mLayoutManager);
        mEmailListRecyclerView.setAdapter(mEmailListAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    getEmails();
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);

        if (PushNotificationStatusHolder.isUpdateNeeded(SharedPrefConstants.PUSH_NOTIFICATION_TAG_EMAIL_UPDATE))
            getEmails();
        else {
            DataHelper dataHelper = DataHelper.getInstance(getActivity());
            String json = dataHelper.getPushEvent(SharedPrefConstants.PUSH_NOTIFICATION_TAG_EMAIL_UPDATE);

            if (json == null)
                getEmails();
            else {
                processGetEmailListResponse(json);
            }
        }

    }

    private void showAddNewEmailDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.add_an_email)
                .autoDismiss(false)
                .customView(R.layout.dialog_add_new_email, true)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .build();

        View view = dialog.getCustomView();

        final EditText emailView = (EditText) view.findViewById(R.id.edit_text_email);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String email = emailView.getText().toString().trim();

                if (!InputValidator.isValidEmail(email)) {
                    emailView.setError(getString(R.string.enter_valid_email));
                    emailView.requestFocus();
                } else {
                    imm.hideSoftInputFromWindow(emailView.getWindowToken(), 0);
                    addNewEmail(email);

                    dialog.dismiss();
                }
            }
        });

        dialog.getBuilder().onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                imm.hideSoftInputFromWindow(emailView.getWindowToken(), 0);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showDeleteEmailConfirmationDialog(final Email email) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.are_you_sure)
                .setMessage(getString(R.string.confirmation_remove_email_address))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEmail(email.getEmailId());
                    }
                })
                .setNegativeButton(android.R.string.no, null);

        dialog.show();
    }

    private void getEmails() {
        if (mGetEmailsTask != null) {
            return;
        }

        mGetEmailsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_EMAILS,
                Constants.BASE_URL_MM + Constants.URL_GET_EMAIL, getActivity(), this);
        mGetEmailsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void addNewEmail(String email) {
        if (mAddNewEmailTask != null) {
            return;
        }

        AddNewEmailRequest addNewEmailRequest = new AddNewEmailRequest(email);
        Gson gson = new Gson();
        String json = gson.toJson(addNewEmailRequest);

        mProgressDialog.setMessage(getString(R.string.progress_dialog_add_email));
        mProgressDialog.show();

        mAddNewEmailTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_NEW_EMAIL,
                Constants.BASE_URL_MM + Constants.URL_POST_EMAIL, json, getActivity(), this);
        mAddNewEmailTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void deleteEmail(long id) {
        if (mDeleteEmailTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_delete_email));
        mProgressDialog.show();

        mDeleteEmailTask = new HttpRequestDeleteAsyncTask(Constants.COMMAND_DELETE_EMAIL,
                Constants.BASE_URL_MM + Constants.URL_DELETE_EMAIL + id, getActivity(), this);
        mDeleteEmailTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void makeEmailPrimary(long id) {
        if (mMakePrimaryEmailTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_make_primary_email));
        mProgressDialog.show();

        MakePrimaryRequest makePrimaryRequest = new MakePrimaryRequest();
        Gson gson = new Gson();
        String json = gson.toJson(makePrimaryRequest);

        mMakePrimaryEmailTask = new HttpRequestPostAsyncTask(Constants.COMMAND_EMAIL_MAKE_PRIMARY,
                Constants.BASE_URL_MM + Constants.URL_POST_EMAIL + id + Constants.URL_MAKE_PRIMARY_EMAIL,
                json, getActivity(), this);
        mMakePrimaryEmailTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        mProgressDialog.dismiss();

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mGetEmailsTask = null;
            mAddNewEmailTask = null;
            mAddNewEmailTask = null;
            mEmailVerificationTask = null;
            mMakePrimaryEmailTask = null;

            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_EMAILS:
                try {
                    mGetEmailResponse = gson.fromJson(result.getJsonString(), GetEmailResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        processGetEmailListResponse(result.getJsonString());

                        DataHelper dataHelper = DataHelper.getInstance(getActivity());
                        dataHelper.updatePushEvents(SharedPrefConstants.PUSH_NOTIFICATION_TAG_EMAIL_UPDATE, result.getJsonString());

                        PushNotificationStatusHolder.setUpdateNeeded(SharedPrefConstants.PUSH_NOTIFICATION_TAG_EMAIL_UPDATE, false);
                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mGetEmailResponse.getMessage(), Toast.LENGTH_LONG).show();
                            ((HomeActivity) getActivity()).switchToDashBoard();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.failed_loading_emails, Toast.LENGTH_LONG).show();
                        ((HomeActivity) getActivity()).switchToDashBoard();
                    }
                }
                mSwipeRefreshLayout.setRefreshing(false);
                mGetEmailsTask = null;
                break;
            case Constants.COMMAND_ADD_NEW_EMAIL:
                try {
                    mAddNewEmailResponse = gson.fromJson(result.getJsonString(), AddNewEmailResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        getEmails();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mAddNewEmailResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mAddNewEmailResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.failed_add_email, Toast.LENGTH_LONG).show();
                    }
                }

                mAddNewEmailTask = null;
                break;
            case Constants.COMMAND_DELETE_EMAIL:
                try {
                    mDeleteEmailResponse = gson.fromJson(result.getJsonString(), DeleteEmailResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        getEmails();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mDeleteEmailResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mDeleteEmailResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.failed_delete_email, Toast.LENGTH_LONG).show();
                    }
                }

                mDeleteEmailTask = null;
                break;
            case Constants.COMMAND_EMAIL_MAKE_PRIMARY:
                try {
                    makePrimaryEmailResponse = gson.fromJson(result.getJsonString(), MakePrimaryEmailResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        getEmails();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), makePrimaryEmailResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), makePrimaryEmailResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.failed_make_primary, Toast.LENGTH_LONG).show();
                    }
                }

                mMakePrimaryEmailTask = null;
                break;
        }

    }

    private void processGetEmailListResponse(String json) {

        try {
            Gson gson = new Gson();
            mGetEmailResponse = gson.fromJson(json, GetEmailResponse.class);

            mEmails = mGetEmailResponse.getEmailAdressList();

            Collections.sort(mEmails, new Comparator<Email>() {
                @Override
                public int compare(Email lhs, Email rhs) {

                    if ((lhs.isPrimary() && !rhs.isPrimary()) || (!lhs.isPrimary() && rhs.isPrimary())) {
                        if (lhs.isPrimary())
                            return -1;
                        else
                            return 1;
                    } else {
                        return (int) (lhs.getEmailId() - rhs.getEmailId());
                    }
                }
            });

            if (mEmails != null && mEmails.size() == 0)
                mEmptyListTextView.setVisibility(View.VISIBLE);
            else mEmptyListTextView.setVisibility(View.GONE);

            if (mEmails.size() > 0 && mEmails.get(0).isPrimary()) {
                mPrimaryEmailViewHeader.setVisibility(View.VISIBLE);
                mPrimaryEmailView.setVisibility(View.VISIBLE);
                mPrimaryVerificationStatus.setVisibility(View.VISIBLE);
                mPrimaryEmailView.setText(mEmails.get(0).getEmailAddress());
                mEmails.remove(0);

            } else {
                mPrimaryEmailViewHeader.setVisibility(View.GONE);
                mPrimaryEmailView.setVisibility(View.GONE);
                mPrimaryVerificationStatus.setVisibility(View.GONE);
            }

            if (mEmails.size() > 0)
                mOtherEmailViewHeader.setVisibility(View.VISIBLE);
            else
                mOtherEmailViewHeader.setVisibility(View.GONE);

            setContentShown(true);

            mEmailListAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public class EmailListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public EmailListAdapter() {
        }

        public class EmailViewHolder extends RecyclerView.ViewHolder {
            private final TextView mEmailView;
            private final ImageView mVerificationStatus;

            private CustomSelectorDialog mCustomSelectorDialog;
            private List<String> mEmailActionList;

            public EmailViewHolder(final View itemView) {
                super(itemView);

                mEmailView = (TextView) itemView.findViewById(R.id.textview_email);
                mVerificationStatus = (ImageView) itemView.findViewById(R.id.email_verification_status);
            }

            public void bindView(int pos) {

                final Email email = mEmails.get(pos);

                final String verificationStatus = email.getVerificationStatus();

                switch (verificationStatus) {
                    case Constants.EMAIL_VERIFICATION_STATUS_VERIFIED:
                        mVerificationStatus.setImageResource(R.drawable.ic_verified);
                        mVerificationStatus.setColorFilter(null);

                        mEmailActionList = Arrays.asList(getResources().getStringArray(R.array.verified_email_action));
                        break;
                    case Constants.EMAIL_VERIFICATION_STATUS_VERIFICATION_IN_PROGRESS:
                        mVerificationStatus.setImageResource(R.drawable.ic_workinprogress);
                        mVerificationStatus.setColorFilter(Color.GRAY);

                        mEmailActionList = Arrays.asList(getResources().getStringArray(R.array.not_verified_email_action));
                        break;
                    default:
                        mVerificationStatus.setImageResource(R.drawable.ic_notverified);
                        mVerificationStatus.setColorFilter(null);

                        mEmailActionList = Arrays.asList(getResources().getStringArray(R.array.not_verified_email_action));
                        break;
                }

                mEmailView.setText(email.getEmailAddress());

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!email.isPrimary()) {
                            mCustomSelectorDialog = new CustomSelectorDialog(getActivity(), email.getEmailAddress(), mEmailActionList);
                            mCustomSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
                                @Override
                                public void onResourceSelected(int selectedIndex, String action) {
                                    if (Constants.ACTION_TYPE_REMOVE.equals(action)) {
                                        showDeleteEmailConfirmationDialog(email);
                                    } else if (Constants.ACTION_TYPE_MAKE_PRIMARY.equals(action)) {
                                        makeEmailPrimary(email.getEmailId());
                                    }
                                }
                            });
                            mCustomSelectorDialog.show();
                        }
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_email,
                    parent, false);

            return new EmailViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                EmailViewHolder vh = (EmailViewHolder) holder;
                vh.bindView(position);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mEmails != null)
                return mEmails.size();
            else return 0;
        }

    }
}
