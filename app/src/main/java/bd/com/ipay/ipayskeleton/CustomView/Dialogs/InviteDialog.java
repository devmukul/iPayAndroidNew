package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.ContactApi.GetContactsAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HomeFragments.ContactsFragments.ContactsHolderFragment;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.SendInviteResponse;
import bd.com.ipay.ipayskeleton.Model.Contact.AddContactRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.Contact.InviteContactNode;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class InviteDialog extends MaterialDialog.Builder implements HttpResponseListener {

    private EditText mEditTextname;
    private EditText mEditTextMobileNumber;
    private EditText mEditTextRelationship;
    private CheckBox mIntroduceCheckbox;

    private String mName;
    private String mMobileNumber;
    private String mRelationship;

    private final Context context;
    private CustomProgressDialog mProgressDialog;

    private HttpRequestPostAsyncTask mAddContactAsyncTask;
    private HttpRequestPostAsyncTask mSendInviteTask = null;
    private SendInviteResponse mSendInviteResponse;

    private ResourceSelectorDialog mResourceSelectorDialog;

    private FinishCheckerListener mFinishCheckerListener;

    public InviteDialog(Context context, String mMobileNumber) {
        super(context);
        this.context = context;
        this.mMobileNumber = mMobileNumber;
        mProgressDialog = new CustomProgressDialog(context);
        showAddContactDialog();
    }

    public void setFinishCheckerListener(FinishCheckerListener finishChceckerListener) {
        mFinishCheckerListener = finishChceckerListener;
    }

    private void showAddContactDialog() {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
        dialog
                .title(R.string.invite_a_contact)
                .autoDismiss(false)
                .customView(R.layout.dialog_invite_contact, true)
                .positiveText(R.string.invite)
                .negativeText(R.string.cancel);

        View dialogView = dialog.build().getCustomView();

        mEditTextname = (EditText) dialogView.findViewById(R.id.edit_text_name);
        mEditTextMobileNumber = (EditText) dialogView.findViewById(R.id.edit_text_mobile_number);
        mEditTextMobileNumber.setText(mMobileNumber);
        mEditTextRelationship = (EditText) dialogView.findViewById(R.id.edit_text_relationship);
        mIntroduceCheckbox = (CheckBox) dialogView.findViewById(R.id.introduceCheckbox);

        mRelationship = null;

        Utilities.showKeyboard(context);

        mResourceSelectorDialog = new ResourceSelectorDialog(context, context.getResources().getString(R.string.relationship), CommonData.getRelationshipList());
        mResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int selectedIndex, String mRelation) {
                mEditTextRelationship.setError(null);
                mEditTextRelationship.setText(mRelation);
                mRelationship = mRelation;
            }
        });

        mEditTextRelationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResourceSelectorDialog.show();
            }
        });

        dialog.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (verifyUserInputs()) {
                    mName = mEditTextname.getText().toString();
                    mMobileNumber = ContactEngine.formatMobileNumberBD(mEditTextMobileNumber.getText().toString());

                    if (mRelationship != null)
                        mRelationship = mRelationship.toUpperCase();

                    addContact(mName, mMobileNumber, mRelationship);

                    Utilities.hideKeyboard(context, mEditTextname);
                    Utilities.hideKeyboard(context, mEditTextMobileNumber);

                    dialog.dismiss();
                }
            }
        });

        dialog.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Utilities.hideKeyboard(context, mEditTextname);
                Utilities.hideKeyboard(context, mEditTextMobileNumber);
                dialog.dismiss();
            }
        });

        dialog.show();
        mEditTextname.requestFocus();
    }

    private boolean verifyUserInputs() {

        boolean error = false;

        String name = mEditTextname.getText().toString();
        String mobileNumber = mEditTextMobileNumber.getText().toString();

        if (name.isEmpty()) {
            mEditTextname.setError(context.getResources().getString(R.string.error_invalid_name));
            error = true;
        }

        if (!InputValidator.isValidNumber(mobileNumber)) {
            mEditTextMobileNumber.setError(context.getResources().getString(R.string.error_invalid_mobile_number));
            error = true;
        }
        return !error;
    }


    private void addContact(String name, String phoneNumber, String relationship) {
        if (mAddContactAsyncTask != null) {
            return;
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        AddContactRequestBuilder addContactRequestBuilder = new
                AddContactRequestBuilder(name, phoneNumber, relationship);

        mAddContactAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_CONTACTS,
                addContactRequestBuilder.generateUri(), addContactRequestBuilder.getAddContactRequest(), context, this, false);
        mAddContactAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendInvite(String phoneNumber) {
        int numberOfInvitees = ContactsHolderFragment.mGetInviteInfoResponse.invitees.size();

        if (numberOfInvitees >= ContactsHolderFragment.mGetInviteInfoResponse.totalLimit) {
            mProgressDialog.dismiss();
            Toast.makeText(context, R.string.invitation_limit_exceeded, Toast.LENGTH_LONG).show();
        } else {
            boolean wantToIntroduce = mIntroduceCheckbox.isChecked();
            InviteContactNode inviteContactNode = new InviteContactNode(phoneNumber, wantToIntroduce);
            Gson gson = new Gson();
            String json = gson.toJson(inviteContactNode, InviteContactNode.class);
            mSendInviteTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SEND_INVITE,
                    Constants.BASE_URL_MM + Constants.URL_SEND_INVITE, json, context, this, false);
            mSendInviteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mAddContactAsyncTask = null;
            mSendInviteTask = null;

            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_ADD_CONTACTS)) {
            try {

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    new GetContactsAsyncTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    sendInvite(mMobileNumber);
                } else {
                    mProgressDialog.dismiss();
                    Toaster.makeText(context, R.string.failed_invite_contact, Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mProgressDialog.dismiss();
                Toaster.makeText(context, R.string.failed_invite_contact, Toast.LENGTH_LONG);
            }

            mAddContactAsyncTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_SEND_INVITE)) {
            mProgressDialog.dismiss();
            try {
                mSendInviteResponse = gson.fromJson(result.getJsonString(), SendInviteResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Toast.makeText(context, R.string.invitation_sent, Toast.LENGTH_LONG).show();

                    if (ContactsHolderFragment.mGetInviteInfoResponse != null)
                        ContactsHolderFragment.mGetInviteInfoResponse.invitees.add(mMobileNumber);

                    if (mFinishCheckerListener != null) {
                        mFinishCheckerListener.ifFinishNeeded();
                    }

                } else {
                    Toaster.makeText(context, mSendInviteResponse.getMessage(), Toast.LENGTH_LONG);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toaster.makeText(context, R.string.failed_sending_invitation, Toast.LENGTH_LONG);
            }
            mSendInviteTask = null;
        }
    }

    public interface FinishCheckerListener {
        void ifFinishNeeded();
    }
}
