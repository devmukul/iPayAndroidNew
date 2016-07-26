package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;


import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.TrustedNetwork.AddTrustedPersonRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.TrustedNetwork.AddTrustedPersonResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;

public class AddTrustedPersonFragment extends Fragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mAddTrustedPersonTask = null;
    private AddTrustedPersonResponse mAddTrustedPersonResponse = null;

    private EditText mEditTextName;
    private EditText mEditTextMobileNumber;
    private EditText mEditTextRelationship;
    private Button mAddButton;

    private CustomSelectorDialog mCustomSelectorDialog;
    private ProgressDialog mProgressDialog;

    private List<String> mRelationshipList;

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

        mProgressDialog = new ProgressDialog(getActivity());

        mRelationshipList = Arrays.asList(getResources().getStringArray(R.array.relationship));
        mCustomSelectorDialog = new CustomSelectorDialog(getActivity(), getString(R.string.relationship), mRelationshipList);

        mEditTextRelationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomSelectorDialog.show();
            }
        });

        mCustomSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int selectedIndex, String mRelation) {
                mEditTextRelationship.setText(mRelation);
                mSelectedRelationId = selectedIndex;
                mRelationship = mRelation;
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInputs()) {
                    AddTrustedPersonRequest addTrustedPersonRequest = new AddTrustedPersonRequest(mName,
                            ContactEngine.formatMobileNumberBD(mMobileNumber), mRelationship.toUpperCase());
                    addTrustedPerson(addTrustedPersonRequest);
                }
            }
        });

        return v;
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;

        mName = mEditTextName.getText().toString();
        mMobileNumber = mEditTextMobileNumber.getText().toString().trim();
        View focusView = null;

        if (mName.isEmpty()) {
            focusView = mEditTextName;
            mEditTextName.setError(getString(R.string.please_enter_valid_mobile_number));
            cancel = true;
        } else if (!ContactEngine.isValidNumber(mMobileNumber)) {

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
        mProgressDialog.setMessage(getString(R.string.progress_dialog_adding_as_trusted_person));
        mProgressDialog.show();

        Gson gson = new Gson();
        String json = gson.toJson(addTrustedPersonRequest);

        mAddTrustedPersonTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_TRUSTED_PERSON,
                Constants.BASE_URL_MM + Constants.URL_POST_TRUSTED_PERSONS, json, getActivity(), this);
        mAddTrustedPersonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setTitle() {
        getActivity().setTitle(R.string.add_a_trusted_person);
    }

    @Override
    public void httpResponseReceiver(HttpResponseObject result) {
        mProgressDialog.dismiss();

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mAddTrustedPersonTask = null;
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_ADD_TRUSTED_PERSON)) {
            try {
                mAddTrustedPersonResponse = gson.fromJson(result.getJsonString(), AddTrustedPersonResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mAddTrustedPersonResponse.getMessage(), Toast.LENGTH_LONG).show();
                    ((SecuritySettingsActivity) getActivity()).switchToPasswordRecovery();
                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mAddTrustedPersonResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.failed_adding_trusted_person, Toast.LENGTH_LONG).show();
            }

            mAddTrustedPersonTask = null;
        }
    }
}