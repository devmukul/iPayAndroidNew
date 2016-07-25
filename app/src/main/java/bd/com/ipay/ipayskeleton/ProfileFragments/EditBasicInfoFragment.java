package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Api.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.BasicInfo.SetProfileInfoRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.BasicInfo.SetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.GetOccupationResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.Occupation;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.OccupationRequestBuilder;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Service.GCM.PushNotificationStatusHolder;
import bd.com.ipay.ipayskeleton.Utilities.Common.GenderList;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class EditBasicInfoFragment extends Fragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mSetProfileInfoTask = null;
    private SetProfileInfoResponse mSetProfileInfoResponse;

    private HttpRequestGetAsyncTask mGetOccupationTask = null;
    private GetOccupationResponse mGetOccupationResponse;

    private ResourceSelectorDialog<Occupation> mOccupationTypeResourceSelectorDialog;

    private EditText mNameEditText;
    private EditText mFathersNameEditText;
    private EditText mMothersNameEditText;
    private EditText mDateOfBirthEditText;
    private EditText mOccupationEditText;
    private CheckBox mFemaleCheckBox;
    private CheckBox mMaleCheckBox;
    private ImageView mDatePickerButton;
    private Button mInfoSaveButton;

    private ProgressDialog mProgressDialog;

    private String mName = "";
    private String mDateOfBirth = "";

    private String mFathersName = "";
    private String mMothersName = "";

    private int mOccupation = -1;
    private String mGender = "";

    private List<Occupation> mOccupationList;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (verifyUserInputs()) {
                Utilities.hideKeyboard(getActivity());
                attemptSaveBasicInfo();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_basic_info, container, false);

        getActivity().setTitle(getString(R.string.edit_basic_info));
        Bundle bundle = getArguments();

        mName = bundle.getString(Constants.NAME);
        mFathersName = bundle.getString(Constants.FATHERS_NAME);
        mMothersName = bundle.getString(Constants.MOTHERS_NAME);
        mDateOfBirth = bundle.getString(Constants.DATE_OF_BIRTH);
        mGender = bundle.getString(Constants.GENDER);
        mOccupation = bundle.getInt(Constants.OCCUPATION);
        mInfoSaveButton = (Button) v.findViewById(R.id.button_save);
        mNameEditText = (EditText) v.findViewById(R.id.name);
        mFathersNameEditText = (EditText) v.findViewById(R.id.fathers_name);
        mMothersNameEditText = (EditText) v.findViewById(R.id.mothers_name);
        mDateOfBirthEditText = (EditText) v.findViewById(R.id.birthdayEditText);
        mOccupationEditText = (EditText) v.findViewById(R.id.occupationEditText);
        mDatePickerButton = (ImageView) v.findViewById(R.id.myDatePickerButton);
        mMaleCheckBox = (CheckBox) v.findViewById(R.id.checkBoxMale);
        mFemaleCheckBox = (CheckBox) v.findViewById(R.id.checkBoxFemale);

        mProgressDialog = new ProgressDialog(getActivity());

        mMaleCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMaleCheckBox.setChecked(true);
                mFemaleCheckBox.setChecked(false);
                mFemaleCheckBox.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                mMaleCheckBox.setTextColor((Color.WHITE));
            }
        });

        mFemaleCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFemaleCheckBox.setChecked(true);
                mMaleCheckBox.setChecked(false);
                mMaleCheckBox.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                mFemaleCheckBox.setTextColor((Color.WHITE));

            }
        });

        mOccupationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOccupationTypeResourceSelectorDialog.show();
            }
        });

        mInfoSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInputs()) {
                    Utilities.hideKeyboard(getActivity());
                    attemptSaveBasicInfo();
                }
            }
        });

        final DatePickerDialog dialog = new DatePickerDialog(
                getActivity(), mDateSetListener, 1990, 0, 1);
        mDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        setProfileInformation();
        getOccupationList();

        return v;
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;
        View focusView = null;

        mName = mNameEditText.getText().toString().trim();
        mDateOfBirth = mDateOfBirthEditText.getText().toString().trim();

        mFathersName = mFathersNameEditText.getText().toString().trim();
        mMothersName = mMothersNameEditText.getText().toString().trim();

        if (mMaleCheckBox.isChecked())
            mGender = GenderList.genderNameToCodeMap.get(
                    getString(R.string.male));

        if (mFemaleCheckBox.isChecked())
            mGender = GenderList.genderNameToCodeMap.get(
                    getString(R.string.female));

        if (mOccupation < 0) {
            mOccupationEditText.setError(getString(R.string.please_enter_occupation));
            return false;
        }

        if (mName.isEmpty()) {
            mNameEditText.setError(getString(R.string.error_invalid_first_name));
            focusView = mNameEditText;
            cancel = true;
        }

        if (!InputValidator.isDateOfBirthValid(mDateOfBirth)) {
            focusView = mDateOfBirthEditText;
            cancel = true;
            mDateOfBirthEditText.setError(getString(R.string.please_enter_valid_date_of_birth));
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void getOccupationList() {
        if (mGetOccupationTask != null) {
            return;
        }

        mGetOccupationTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_OCCUPATIONS_REQUEST,
                new OccupationRequestBuilder().getGeneratedUri(), getActivity(), this);
        mGetOccupationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptSaveBasicInfo() {
        mProgressDialog.setMessage(getString(R.string.saving_profile_information));
        mProgressDialog.show();

        Gson gson = new Gson();

        SetProfileInfoRequest setProfileInfoRequest = new SetProfileInfoRequest(mName, mGender, mDateOfBirth,
                mOccupation, mFathersName, mMothersName);

        String profileInfoJson = gson.toJson(setProfileInfoRequest);
        mSetProfileInfoTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_PROFILE_INFO_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_SET_PROFILE_INFO_REQUEST, profileInfoJson, getActivity(), this);
        mSetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setProfileInformation() {

        mNameEditText.setText(mName);

        mFathersNameEditText.setText(mFathersName);
        mMothersNameEditText.setText(mMothersName);

        mDateOfBirthEditText.setText(mDateOfBirth);

        String[] genderArray = GenderList.genderNames;

        if (mGender.equals(GenderList.genderNameToCodeMap.get(
                genderArray[0]))) {
            mMaleCheckBox.setChecked(true);
            mFemaleCheckBox.setChecked(false);
            mFemaleCheckBox.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            mMaleCheckBox.setTextColor((Color.WHITE));
        } else if (mGender.equals(GenderList.genderNameToCodeMap.get(
                genderArray[1]))) {
            mMaleCheckBox.setChecked(false);
            mFemaleCheckBox.setChecked(true);
            mMaleCheckBox.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            mFemaleCheckBox.setTextColor((Color.WHITE));
        }
    }

    private final DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mDateOfBirthEditText.setText(
                            String.format("%02d/%02d/%4d", dayOfMonth, monthOfYear + 1, year));
                }
            };

    public void httpResponseReceiver(HttpResponseObject result) {
        mProgressDialog.dismiss();
        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mSetProfileInfoTask = null;
            mGetOccupationTask = null;
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
            return;
        }


        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_SET_PROFILE_INFO_REQUEST:

                try {
                    mSetProfileInfoResponse = gson.fromJson(result.getJsonString(), SetProfileInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mSetProfileInfoResponse.getMessage(), Toast.LENGTH_LONG).show();

                            // We need to update the basic info page when user navigates to that page from the current edit page.
                            // But by default, the basic info stored in our database is refreshed only when a push is received.
                            // It might be the case that push notification is not yet received on the phone and user already
                            // navigated to the basic info page. To handle this case, we are setting updateNeeded to true.
                            PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_PROFILE_INFO_UPDATE, true);

                            getActivity().onBackPressed();
                        }
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();
                }

                mSetProfileInfoTask = null;
                break;

            case Constants.COMMAND_GET_OCCUPATIONS_REQUEST:

                try {
                    mGetOccupationResponse = gson.fromJson(result.getJsonString(), GetOccupationResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mOccupationList = mGetOccupationResponse.getOccupations();

                        setOccupationAdapter(mOccupationList);

                        for (int i = 0; i < mOccupationList.size(); i++) {
                            if (mOccupationList.get(i).getId() == mOccupation) {
                                String occupation = mGetOccupationResponse.getOccupation(mOccupation);
                                if (occupation != null) {
                                    mOccupationEditText.setText(occupation);
                                }

                                break;
                            }
                        }

                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.failed_loading_occupation_list, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.failed_loading_occupation_list, Toast.LENGTH_LONG).show();
                }

                mGetOccupationTask = null;
                break;
        }
    }

    private void setOccupationAdapter(List<Occupation> occupationList) {
        mOccupationTypeResourceSelectorDialog = new ResourceSelectorDialog<>(getActivity(),getString(R.string.occupation), occupationList, mOccupation);
        mOccupationTypeResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int id, String name) {
                mOccupationEditText.setText(name);
                mOccupation = id;
            }
        });
    }
}
