package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.ContactPickerDialogActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetParentInfoRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetParentInfoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BulkSignUpHelperDialog;

public class EditParentInfoFragment extends BaseFragment implements HttpResponseListener {

    private final int PICK_FATHERS_MOBILE_NUMBER_REQUEST = 100;
    private final int PICK_MOTHERS_MOBILE_NUMBER_REQUEST = 101;
    private HttpRequestPostAsyncTask mSetParentInfoTask = null;
    private SetParentInfoResponse mSetParentInfoResponse;
    private EditText mFathersNameEditText;
    private EditText mMothersNameEditText;
    private EditText mFathersMobileEditText;
    private EditText mMothersMobileEditText;
    private ImageView mSelectFatherMobileContactButton;
    private ImageView mSelectMotherMobileContactButton;
    private CustomProgressDialog mProgressDialog;
    private String mFathersName = "";
    private String mMothersName = "";
    private String mFathersMobile = "";
    private String mMothersMobile = "";
    private Button mInfoSaveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_parent_info, container, false);

        getActivity().setTitle(getString(R.string.edit_parent_info));
        Bundle bundle = getArguments();

        mFathersName = bundle.getString(Constants.FATHERS_NAME);
        mMothersName = bundle.getString(Constants.MOTHERS_NAME);
        mFathersMobile = bundle.getString(Constants.FATHERS_MOBILE);
        mMothersMobile = bundle.getString(Constants.MOTHERS_MOBILE);

        mFathersNameEditText = (EditText) view.findViewById(R.id.fathers_name);
        mMothersNameEditText = (EditText) view.findViewById(R.id.mothers_name);
        mFathersMobileEditText = (EditText) view.findViewById(R.id.fathers_mobile);
        mMothersMobileEditText = (EditText) view.findViewById(R.id.mothers_mobile);
        mSelectFatherMobileContactButton = (ImageView) view.findViewById(R.id.father_number);
        mSelectMotherMobileContactButton = (ImageView) view.findViewById(R.id.mother_number);

        mInfoSaveButton = (Button) view.findViewById(R.id.button_save);

        mProgressDialog = new CustomProgressDialog(getActivity());


        setParentInformation();

        mSelectFatherMobileContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactPickerDialogActivity.class);
                startActivityForResult(intent, PICK_FATHERS_MOBILE_NUMBER_REQUEST);
            }
        });

        mSelectMotherMobileContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactPickerDialogActivity.class);
                startActivityForResult(intent, PICK_MOTHERS_MOBILE_NUMBER_REQUEST);
            }
        });

        mInfoSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInputs()) {
                    Utilities.hideKeyboard(getActivity());
                    attemptSaveParentInfo();
                }
            }
        });

        if(!BulkSignupUserDetailsCacheManager.isBasicInfoChecked(true)){
            final String cacheFathersName = BulkSignupUserDetailsCacheManager.getFatherName(null);
            final String cacheFathersMobile = BulkSignupUserDetailsCacheManager.getFatherMobile(null);
            final String cacheMothersName = BulkSignupUserDetailsCacheManager.getMotherName(null);
            final String cacheMothersMobile = BulkSignupUserDetailsCacheManager.getMotherMobile(null);

            if(!TextUtils.isEmpty(cacheFathersName) || !TextUtils.isEmpty(cacheFathersMobile) || !TextUtils.isEmpty(cacheMothersName) || !TextUtils.isEmpty(cacheMothersMobile)) {

                final BulkSignUpHelperDialog bulkSignUpHelperDialog = new BulkSignUpHelperDialog(getContext(),
                        getString(R.string.bulk_signup_basic_info_helper_msg));

                bulkSignUpHelperDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mFathersNameEditText.setText(cacheFathersName);
                        mFathersMobileEditText.setText(cacheFathersMobile);
                        mMothersNameEditText.setText(cacheMothersName);
                        mMothersMobileEditText.setText(cacheMothersMobile);
                        bulkSignUpHelperDialog.cancel();
                    }
                });

                bulkSignUpHelperDialog.setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bulkSignUpHelperDialog.cancel();
                        bulkSignUpHelperDialog.setCheckedResponse("BasicInfo");
                    }
                });

                bulkSignUpHelperDialog.show();
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_parent_info_edit));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FATHERS_MOBILE_NUMBER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String mobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);
                if (mobileNumber != null)
                    mFathersMobileEditText.setText(mobileNumber);
                mFathersMobileEditText.setError(null);
            }
        } else if (requestCode == PICK_MOTHERS_MOBILE_NUMBER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String mobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);
                if (mobileNumber != null)
                    mMothersMobileEditText.setText(mobileNumber);
                mMothersMobileEditText.setError(null);
            }
        }
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;
        View focusView = null;

        mFathersName = mFathersNameEditText.getText().toString().trim();
        mMothersName = mMothersNameEditText.getText().toString().trim();

        mFathersMobile = ContactEngine.formatMobileNumberBD(mFathersMobileEditText.getText().toString().trim());
        mMothersMobile = ContactEngine.formatMobileNumberBD(mMothersMobileEditText.getText().toString().trim());

        if (mFathersName.isEmpty()) {
            mFathersNameEditText.setError(getString(R.string.error_invalid_first_name));
            focusView = mFathersNameEditText;
            cancel = true;
        } else if (!InputValidator.isValidNameWithRequiredLength(mFathersName)) {
            mFathersNameEditText.setError(getString(R.string.error_invalid_name_with_required_length));
            focusView = mFathersNameEditText;
            cancel = true;
        }

        if (mMothersName.isEmpty()) {
            mMothersNameEditText.setError(getString(R.string.error_invalid_first_name));
            focusView = mMothersNameEditText;
            cancel = true;
        } else if (!InputValidator.isValidNameWithRequiredLength(mMothersName)) {
            mMothersNameEditText.setError(getString(R.string.error_invalid_name_with_required_length));
            focusView = mMothersNameEditText;
            cancel = true;
        }

        if (mFathersMobile.isEmpty()) mFathersMobile = null;
        else if (!InputValidator.isValidNumber(mFathersMobile)) {
            mFathersMobileEditText.setError(getString(R.string.error_invalid_mobile_number));
            focusView = mFathersMobileEditText;
            cancel = true;
        }

        if (mMothersMobile.isEmpty()) mMothersMobile = null;
        else if (!InputValidator.isValidNumber(mMothersMobile)) {
            mMothersMobileEditText.setError(getString(R.string.error_invalid_mobile_number));
            focusView = mMothersMobileEditText;
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void attemptSaveParentInfo() {
        mProgressDialog.show();

        Gson gson = new Gson();

        SetParentInfoRequest setParentInfoRequest = new SetParentInfoRequest(mFathersMobile, mMothersMobile, mFathersName, mMothersName);

        String parentInfoJson = gson.toJson(setParentInfoRequest);
        mSetParentInfoTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_PARENT_INFO_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_SET_PARENT_INFO_REQUEST, parentInfoJson, getActivity(), this, false);
        mSetParentInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setParentInformation() {

        mFathersNameEditText.setText(mFathersName);
        mMothersNameEditText.setText(mMothersName);
        mFathersMobileEditText.setText(mFathersMobile);
        mMothersMobileEditText.setText(mMothersMobile);

    }

    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mSetParentInfoTask = null;
            return;
        }


        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_SET_PARENT_INFO_REQUEST:

                try {
                    mSetParentInfoResponse = gson.fromJson(result.getJsonString(), SetParentInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mSetParentInfoResponse.getMessage(), Toast.LENGTH_LONG).show();

                            getActivity().onBackPressed();

                            //Google Analytic event
                            Utilities.sendSuccessEventTracker(mTracker, "Parent Info Update", ProfileInfoCacheManager.getAccountId());
                        }
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.parent_info_save_failed, Toast.LENGTH_SHORT).show();

                        //Google Analytic event
                        Utilities.sendFailedEventTracker(mTracker, "Parent Info Update", ProfileInfoCacheManager.getAccountId(), mSetParentInfoResponse.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.parent_info_save_failed, Toast.LENGTH_SHORT).show();

                    //Google Analytic event
                    Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
                }

                mSetParentInfoTask = null;
                break;

        }
    }
}
