package bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.ProfileVerificationHelperActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.AddressInputOnboardView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.SetUserAddressRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.SetUserAddressResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetProfileInfoRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetOccupationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Occupation;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.OccupationRequestBuilder;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Widget.View.BulkSignUpHelperDialog;

public class OnBoardAddBasicInfoFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetOccupationTask = null;
    private GetOccupationResponse mGetOccupationResponse;
    private HttpRequestPostAsyncTask mSetProfileInfoTask = null;

    private HttpRequestPostAsyncTask mSetUserAddressTask = null;

    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetProfileInfoResponse mGetProfileInfoResponse;

    private ResourceSelectorDialog<Occupation> mOccupationTypeResourceSelectorDialog;
    private AddressClass mPresentAddress;
    private AddressInputOnboardView mAddressInputView;
    private CustomProgressDialog mProgressDialog;

    private EditText mOccupationEditText;
    private EditText mOrganizationNameEditText;
    private String mGender = null;
    private String mOrganizationName = null;
    private int mOccupation = -1;
    private List<Occupation> mOccupationList;

    private Button mSkipButton;
    private ImageView mBackButtonTop;
    private EditText mAddressLine1Field;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboard_add_basic_info, container, false);
        mProgressDialog = new CustomProgressDialog(getActivity());
        mAddressInputView = (AddressInputOnboardView) v.findViewById(R.id.input_address);
        Button mSaveButton = (Button) v.findViewById(R.id.button_upload_profile_pic);

        mSkipButton = (Button) v.findViewById(R.id.button_skip);

        mOccupationEditText = (EditText) v.findViewById(R.id.occupationEditText);
        mOrganizationNameEditText = (EditText) v.findViewById(R.id.organizationNameEditText);
        mBackButtonTop = (ImageView) v.findViewById(R.id.back);
        mAddressLine1Field = (EditText) mAddressInputView.findViewById(R.id.address_line_1);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mAddressInputView.clearFocus();
        mOrganizationNameEditText.clearFocus();

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (verifyUserInputs() && mAddressInputView.verifyUserInputs()) {
                    mPresentAddress = mAddressInputView.getInformation();
                    attemptSaveBasicInfo();
                }
            }
        });

        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ProfileInfoCacheManager.isSourceOfFundAdded()) {
                    ((ProfileVerificationHelperActivity) getActivity()).switchToSourceOfFundHelperFragment();
                } else {
                    ((ProfileVerificationHelperActivity) getActivity()).switchToHomeActivity();
                }

            }
        });

        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            mBackButtonTop.setVisibility(View.INVISIBLE);
        }

        mBackButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        getProfileInfo();
        getOccupationList();

        boolean isChecked = BulkSignupUserDetailsCacheManager.isBasicInfoChecked(true);

        if(!isChecked){
            final String cachePresentAddress = BulkSignupUserDetailsCacheManager.getPresentAddress(null);
            final String cacheOrganizationName = BulkSignupUserDetailsCacheManager.getOrganizationName(null);

            if(!TextUtils.isEmpty(cachePresentAddress) || !TextUtils.isEmpty(cacheOrganizationName)) {
                final BulkSignUpHelperDialog bulkSignUpHelperDialog = new BulkSignUpHelperDialog(getContext(),
                        getString(R.string.bulk_signup_basic_info_helper_msg));

                bulkSignUpHelperDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAddressLine1Field.setText(cachePresentAddress);
                        mOrganizationNameEditText.setText(cacheOrganizationName);
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

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void setUserAddress() {
        SetUserAddressRequest userAddressRequest = new SetUserAddressRequest(Constants.ADDRESS_TYPE_PRESENT, mPresentAddress);

        Gson gson = new Gson();
        String addressJson = gson.toJson(userAddressRequest, SetUserAddressRequest.class);
        mSetUserAddressTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_USER_ADDRESS_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_SET_USER_ADDRESS_REQUEST, addressJson, getActivity(), this, false);
        mSetUserAddressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getProfileInfo() {
        if (mGetProfileInfoTask != null) {
            return;
        }
        mProgressDialog.show();
        mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_INFO_REQUEST, getActivity(), this, false);
        mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptSaveBasicInfo() {
        Gson gson = new Gson();
        mProgressDialog.show();

        SetProfileInfoRequest setProfileInfoRequest = new SetProfileInfoRequest(ProfileInfoCacheManager.getUserName(), mGender, ProfileInfoCacheManager.getBirthday(),
                mOccupation, mOrganizationName);
        String profileInfoJson = gson.toJson(setProfileInfoRequest);
        mSetProfileInfoTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_PROFILE_INFO_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_SET_PROFILE_INFO_REQUEST, profileInfoJson, getActivity(), this, false);
        mSetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mSetUserAddressTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SET_USER_ADDRESS_REQUEST)) {

            try {
                SetUserAddressResponse mSetUserAddressResponse = gson.fromJson(result.getJsonString(), SetUserAddressResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Toast.makeText(getActivity(), mSetUserAddressResponse.getMessage(), Toast.LENGTH_LONG).show();
                    ProfileInfoCacheManager.addBasicInfo(true);
                    getActivity().getSupportFragmentManager().popBackStack();
                    if (ProfileInfoCacheManager.isSourceOfFundAdded()) {
                        ((ProfileVerificationHelperActivity) getActivity()).switchToHomeActivity();
                    } else {
                        ((ProfileVerificationHelperActivity) getActivity()).switchToSourceOfFundHelperFragment();
                    }

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mSetUserAddressResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();
            }
            mProgressDialog.dismiss();
            mSetUserAddressTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_OCCUPATIONS_REQUEST)) {

            try {
                mGetOccupationResponse = gson.fromJson(result.getJsonString(), GetOccupationResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mOccupationList = mGetOccupationResponse.getOccupations();
                    setOccupationAdapter();
                    setOccupation();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mGetOccupationTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_SET_PROFILE_INFO_REQUEST)) {
            try {
                SetProfileInfoResponse mSetProfileInfoResponse = gson.fromJson(result.getJsonString(), SetProfileInfoResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        setUserAddress();
                    }
                } else {
                    if (getActivity() != null) {
                        mProgressDialog.dismiss();
                        Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();
                }
            }

            mSetProfileInfoTask = null;
        }
        if (result.getApiCommand().equals(Constants.COMMAND_GET_PROFILE_INFO_REQUEST)) {

            try {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    processProfileInfoResponse(result.getJsonString());
                } else {
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
            }

            mProgressDialog.dismiss();
            mGetProfileInfoTask = null;
        }
    }


    private void setOccupation() {

        for (int i = 0; i < mOccupationList.size(); i++) {
            if (mOccupationList.get(i).getId() == mOccupation) {
                String occupation = mOccupationList.get(i).getName();
                if (occupation != null) {
                    mOccupationEditText.setText(occupation);
                }

                break;
            }
        }
    }

    private void setOccupationAdapter() {
        mOccupationTypeResourceSelectorDialog = new ResourceSelectorDialog<>(getActivity(), getString(R.string.select_an_occupation), mOccupationList);
        mOccupationTypeResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int id, String name) {
                mOccupationEditText.setText(name);
                mOccupation = id;
            }
        });

        mOccupationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOccupationTypeResourceSelectorDialog.show();
            }
        });
    }


    private void getOccupationList() {
        if (mGetOccupationTask != null) {
            return;
        }
        mGetOccupationTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_OCCUPATIONS_REQUEST,
                new OccupationRequestBuilder().getGeneratedUri(), getActivity(), this, true);
        mGetOccupationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void processProfileInfoResponse(String json) {
        Gson gson = new Gson();
        mGetProfileInfoResponse = gson.fromJson(json, GetProfileInfoResponse.class);

        mOrganizationName = mGetProfileInfoResponse.getOrganizationName();
        mOccupation = mGetProfileInfoResponse.getOccupation();

        setProfileInformation();
        setOccupation();
    }

    private void setProfileInformation() {

        if (mOrganizationName != null && !mOrganizationName.isEmpty())
            mOrganizationNameEditText.setText(mOrganizationName);
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;
        View focusView = null;
        mOrganizationName = mOrganizationNameEditText.getText().toString().trim();

        if (mOrganizationName.isEmpty())
            mOrganizationName = null;

        if (mOccupation < 0) {
            mOccupationEditText.setError(getString(R.string.please_enter_occupation));
            return false;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

}