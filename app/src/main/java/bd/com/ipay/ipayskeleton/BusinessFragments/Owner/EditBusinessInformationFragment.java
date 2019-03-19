package bd.com.ipay.ipayskeleton.BusinessFragments.Owner;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Owner.SetBusinessInformationRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Owner.SetBusinessInformationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class EditBusinessInformationFragment extends Fragment implements HttpResponseListener {

    private EditText mBusinessNameEditText;
    private EditText mCompanyNameEditText;
    private EditText mBusinessMobileNumberEditText;
    private EditText mBusinessTypeEditText;
    private Button mInfoSaveButton;

    private String mBusinessName;
    private String mCompanyName;
    private String mBusinessMobileNumber;
    private int mBusinessTypeId;
    private ArrayList<BusinessType> mBusinessTypes;

    private ResourceSelectorDialog<BusinessType> businessTypeResourceSelectorDialog;

    private HttpRequestPostAsyncTask mSetBusinessInformationRequestAsyncTask;
    private SetBusinessInformationResponse mSetBusinessInformationResponse;

    private CustomProgressDialog mProgressDialog;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_business_information_edit));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_business_information, container, false);
        getActivity().setTitle(R.string.edit_business_information);

        mBusinessNameEditText = (EditText) v.findViewById(R.id.business_name);
        mCompanyNameEditText = (EditText) v.findViewById(R.id.company_name);
        mBusinessMobileNumberEditText = (EditText) v.findViewById(R.id.business_mobile_number);
        mBusinessTypeEditText = (EditText) v.findViewById(R.id.business_type);
        mInfoSaveButton = (Button) v.findViewById(R.id.button_save);

        mBusinessName = getArguments().getString(Constants.BUSINESS_NAME);
        mCompanyName = getArguments().getString(Constants.COMPANY_NAME);
        System.out.println("Company Name "+mCompanyName);

        mBusinessMobileNumber = getArguments().getString(Constants.BUSINESS_MOBILE_NUMBER);
        mBusinessTypeId = getArguments().getInt(Constants.BUSINESS_TYPE);
        mBusinessTypes = getArguments().getParcelableArrayList(Constants.BUSINESS_TYPE_LIST);

        businessTypeResourceSelectorDialog = new ResourceSelectorDialog<>(getActivity(), getString(R.string.business_type), mBusinessTypes);
        businessTypeResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int id, String name) {
                mBusinessTypeEditText.setError(null);
                mBusinessTypeEditText.setText(name);
                mBusinessTypeId = id;
            }
        });

        mBusinessTypeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                businessTypeResourceSelectorDialog.show();
            }
        });

        mBusinessNameEditText.setText(mBusinessName);
        mCompanyNameEditText.setText(mCompanyName);
        mBusinessMobileNumberEditText.setText(mBusinessMobileNumber);
        for (BusinessType businessType : mBusinessTypes) {
            if (businessType.getId() == mBusinessTypeId) {
                mBusinessTypeEditText.setText(businessType.getName());
                break;
            }
        }

        mProgressDialog = new CustomProgressDialog(getActivity());

        mInfoSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInputs()) {
                    Utilities.hideKeyboard(getActivity());
                    attemptSaveBusinessInformation();
                }
            }
        });

        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void attemptSaveBusinessInformation() {
        if (mSetBusinessInformationRequestAsyncTask != null)
            return;

        mProgressDialog.dismiss();

        Gson gson = new Gson();
        SetBusinessInformationRequest setBusinessInformationRequest = new SetBusinessInformationRequest(
                mBusinessName, mCompanyName, mBusinessTypeId, mBusinessMobileNumber);
        String json = gson.toJson(setBusinessInformationRequest);

        mSetBusinessInformationRequestAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_BUSINESS_INFORMATION,
                Constants.BASE_URL_MM + Constants.URL_SET_BUSINESS_INFORMATION, json, getActivity(), this, false);
        mSetBusinessInformationRequestAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;
        View focusView = null;

        mBusinessName = mBusinessNameEditText.getText().toString().trim();
        mCompanyName = mCompanyNameEditText.getText().toString().trim();
        mBusinessMobileNumber = ContactEngine.formatMobileNumberBD(mBusinessMobileNumberEditText.getText().toString());
        /** mBusinessTypeId has already been selected when the user picked an item from the dialog **/

        if (mBusinessName.isEmpty()) {
            mBusinessNameEditText.setError(getString(R.string.business_name_required));
            cancel = true;
            focusView = mBusinessNameEditText;
        }
        if (!InputValidator.isValidNumber(mBusinessMobileNumber)) {
            mBusinessMobileNumberEditText.setError(getString(R.string.error_invalid_mobile_number));
            cancel = true;
            focusView = mBusinessMobileNumberEditText;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mSetBusinessInformationRequestAsyncTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SET_BUSINESS_INFORMATION)) {
            try {
                mSetBusinessInformationResponse = gson.fromJson(result.getJsonString(), SetBusinessInformationResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toaster.makeText(getActivity(), mSetBusinessInformationResponse.getMessage(), Toast.LENGTH_LONG);
                        ((ProfileActivity) getActivity()).switchToBusinessInfoFragment();
                    }
                } else {
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), mSetBusinessInformationResponse.getMessage(), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.business_information_saving_failed, Toast.LENGTH_LONG);
            }

            mSetBusinessInformationRequestAsyncTask = null;
        }
    }
}
