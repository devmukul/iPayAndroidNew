package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.ContactPickerDialogActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.TrustedNetwork.AddTrustedPersonRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.TrustedNetwork.AddTrustedPersonResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class AddTrustedPersonFragment extends BaseFragment implements HttpResponseListener {

    private final int PICK_CONTACT_REQUEST = 100;
    private HttpRequestPostAsyncTask mAddTrustedPersonTask = null;
    private AddTrustedPersonResponse mAddTrustedPersonResponse = null;
    private EditText mEditTextName;
    private EditText mEditTextMobileNumber;
    private EditText mEditTextRelationship;
    private ImageView mSelectContactButton;
    private Button mAddButton;
    private ResourceSelectorDialog mResourceSelectorDialog;
    private CustomProgressDialog mProgressDialog;
    private String mName;
    private String mRelationship;
    private String mMobileNumber;
    private int mSelectedRelationId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_trusted_person, container, false);
        setTitle();

        mEditTextName = (EditText) v.findViewById(R.id.edit_text_name);
        mEditTextMobileNumber = (EditText) v.findViewById(R.id.edit_text_mobile_number);
        mEditTextRelationship = (EditText) v.findViewById(R.id.edit_text_relationship);
        mAddButton = (Button) v.findViewById(R.id.button_add_trusted_person);
        mSelectContactButton = (ImageView) v.findViewById(R.id.select_number_from_contacts);

        mProgressDialog = new CustomProgressDialog(getActivity());

        mEditTextName.requestFocus();
        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


        mResourceSelectorDialog = new ResourceSelectorDialog(getActivity(), getString(R.string.relationship), CommonData.getRelationshipList());
        mResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int selectedIndex, String mRelation) {
                mEditTextRelationship.setError(null);
                mEditTextRelationship.setText(mRelation);
                mSelectedRelationId = selectedIndex;
                mRelationship = mRelation;
            }
        });

        mEditTextRelationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResourceSelectorDialog.show();
            }
        });

        mSelectContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactPickerDialogActivity.class);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.MANAGE_TRUSTED_PERSON)
            public void onClick(View v) {
                Utilities.hideKeyboard(getActivity());
                if (verifyUserInputs()) {
                    AddTrustedPersonRequest addTrustedPersonRequest = new AddTrustedPersonRequest(mName,
                            ContactEngine.formatMobileNumberBD(mMobileNumber), mRelationship.toUpperCase());
                    addTrustedPerson(addTrustedPersonRequest);
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_add_trusted_person));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String mobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);
                if (mobileNumber != null)
                    mEditTextMobileNumber.setText(mobileNumber);
                mEditTextMobileNumber.setError(null);
            }
        }
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;

        mName = mEditTextName.getText().toString();
        mMobileNumber = mEditTextMobileNumber.getText().toString().trim();
        View focusView = null;

        if (mName.isEmpty()) {
            focusView = mEditTextName;
            mEditTextName.setError(getString(R.string.error_invalid_name));
            cancel = true;
        } else if (!InputValidator.isValidNumber(mMobileNumber)) {

            focusView = mEditTextMobileNumber;
            mEditTextMobileNumber.setError(getString(R.string.please_enter_valid_mobile_number));
            cancel = true;
        } else if (mSelectedRelationId < 0) {
            mEditTextRelationship.setError(getString(R.string.please_select_relation));
            cancel = true;
        }
        if (cancel) {
            if (focusView != null)
                focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void addTrustedPerson(AddTrustedPersonRequest addTrustedPersonRequest) {
        if (mAddTrustedPersonTask != null) {
            return;
        }
        mProgressDialog.show();

        Gson gson = new Gson();
        String json = gson.toJson(addTrustedPersonRequest);

        mAddTrustedPersonTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_TRUSTED_PERSON,
                Constants.BASE_URL_MM + Constants.URL_POST_TRUSTED_PERSONS, json, getActivity(), this, false);
        mAddTrustedPersonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setTitle() {
        getActivity().setTitle(R.string.add_a_trusted_person);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mAddTrustedPersonTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_ADD_TRUSTED_PERSON)) {
            try {
                mAddTrustedPersonResponse = gson.fromJson(result.getJsonString(), AddTrustedPersonResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mAddTrustedPersonResponse.getMessage(), Toast.LENGTH_LONG).show();
                    ((SecuritySettingsActivity) getActivity()).switchToTrustedPersonFragment();

                    //Google Analytic event
                    Utilities.sendSuccessEventTracker(mTracker, "Add Trusted Person", ProfileInfoCacheManager.getAccountId());
                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mAddTrustedPersonResponse.getMessage(), Toast.LENGTH_LONG).show();

                    //Google Analytic event
                    Utilities.sendFailedEventTracker(mTracker, "Trusted Person Update", ProfileInfoCacheManager.getAccountId(), mAddTrustedPersonResponse.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.failed_adding_trusted_person, Toast.LENGTH_LONG);

                //Google Analytic event
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
            }

            mAddTrustedPersonTask = null;
        }
    }
}
