package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetParentInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetOccupationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Occupation;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.OccupationRequestBuilder;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.GenderList;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class BasicInfoFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetProfileInfoResponse mGetProfileInfoResponse;

    private HttpRequestGetAsyncTask mGetParentInfoTask = null;
    private GetParentInfoResponse mGetParentInfoResponse;

    private HttpRequestGetAsyncTask mGetOccupationTask = null;
    private GetOccupationResponse mGetOccupationResponse;

    private List<Occupation> mOccupationList;

    private CustomProgressDialog mProgressDialog;

    private TextView mNameView;
    private TextView mMobileNumberView;
    private TextView mVerificationStatusView;

    private TextView mFathersNameView;
    private TextView mFathersMobileView;
    private TextView mMothersNameView;
    private TextView mMothersMobileView;

    private TextView mDateOfBirthView;
    private TextView mOccupationView;
    private TextView mOrganizationNameView;
    private TextView mGenderView;
    private TextView mSignUpTimeView;

    private String mName = "";
    private String mMobileNumber = "";
    private String mProfileImageUrl = "";

    private String mDateOfBirth = "";

    private String mFathersName = "";
    private String mMothersName = "";
    private String mFathersMobile = "";
    private String mMothersMobile = "";
    private String occupation = "";
    private int mOccupation = 0;
    private String mOrganizationName;
    private String mGender;
    private String mSignUpTime = "";
    private String mVerificationStatus = null;

    private ImageButton mContactEditButton;
    private ImageButton mParentInfoEditButton;

    private View mUserInformationHolder;
    private View mParentInformationHolder;

    private TextView mUserInfoServiceNotAllowedTextView;
    private TextView mParentInfoServiceNotAllowedTextView;
    private Tracker mTracker;

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_user_basic_info));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_basic_info, container, false);
        getActivity().setTitle(R.string.basic_info);

        mNameView = (TextView) view.findViewById(R.id.textview_name);
        mMobileNumberView = (TextView) view.findViewById(R.id.textview_mobile_number);
        mVerificationStatusView = (TextView) view.findViewById(R.id.textview_verification_status);
        mFathersNameView = (TextView) view.findViewById(R.id.textview_fathers_name);
        mFathersMobileView = (TextView) view.findViewById(R.id.textview_fathers_mobile);
        mMothersNameView = (TextView) view.findViewById(R.id.textview_mothers_name);
        mMothersMobileView = (TextView) view.findViewById(R.id.textview_mothers_mobile);
        mDateOfBirthView = (TextView) view.findViewById(R.id.textview_dob);
        mOccupationView = (TextView) view.findViewById(R.id.textview_occupation);
        mOrganizationNameView = (TextView) view.findViewById(R.id.textview_organization_name);
        mGenderView = (TextView) view.findViewById(R.id.textview_gender);
        mSignUpTimeView = (TextView) view.findViewById(R.id.textview_signup);
        mMobileNumber = ProfileInfoCacheManager.getMobileNumber();
        mDateOfBirth = ProfileInfoCacheManager.getBirthday();
        mProgressDialog = new CustomProgressDialog(getActivity());
        mContactEditButton = (ImageButton) view.findViewById(R.id.button_edit_contact_information);
        mParentInfoEditButton = (ImageButton) view.findViewById(R.id.button_edit_parent_information);
        mUserInformationHolder = view.findViewById(R.id.user_information_holder);
        mParentInformationHolder = view.findViewById(R.id.parent_information_holder);
        mUserInfoServiceNotAllowedTextView = (TextView) view.findViewById(R.id.user_info_service_not_allowed_text_view);
        mParentInfoServiceNotAllowedTextView = (TextView) view.findViewById(R.id.parent_info_service_not_allowed_text_view);

        mContactEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.MANAGE_PROFILE)
            public void onClick(View v) {
                if (mOccupationList != null) {
                    launchEditFragment();
                } else {
                    Toast.makeText(getContext(), "Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mParentInfoEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.MANAGE_PARENT)
            public void onClick(View v) {
                launchEditParentInfoFragment();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setContentShown(false);
        getProfileInfo();
        getParentInfo();
    }

    private void launchEditFragment() {
        Bundle bundle = new Bundle();

        bundle.putString(Constants.MOBILE_NUMBER, mMobileNumber);
        bundle.putString(Constants.NAME, mName);
        bundle.putString(Constants.DATE_OF_BIRTH, mDateOfBirth);
        bundle.putString(Constants.PROFILE_PICTURE, mProfileImageUrl);
        bundle.putString(Constants.GENDER, mGender);
        bundle.putInt(Constants.OCCUPATION, mOccupation);
        bundle.putString(Constants.ORGANIZATION_NAME, mOrganizationName);
        bundle.putParcelableArrayList(Constants.OCCUPATION_LIST, new ArrayList<>(mOccupationList));
        ((ProfileActivity) getActivity()).switchToEditBasicInfoFragment(bundle);
    }


    private void launchEditParentInfoFragment() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FATHERS_NAME, mFathersName);
        bundle.putString(Constants.MOTHERS_NAME, mMothersName);
        bundle.putString(Constants.FATHERS_MOBILE, ContactEngine.formatMobileNumberBD(mFathersMobile));
        bundle.putString(Constants.MOTHERS_MOBILE, ContactEngine.formatMobileNumberBD(mMothersMobile));

        ((ProfileActivity) getActivity()).switchToEditParentInfoFragment(bundle);
    }

    private void setProfileInformation() {
        mMobileNumberView.setText(mMobileNumber);
        mNameView.setText(mName);
        mDateOfBirthView.setText(mDateOfBirth);
        mSignUpTimeView.setText(mSignUpTime);

        if (mOrganizationName != null && !mOrganizationName.isEmpty())
            mOrganizationNameView.setText(mOrganizationName);

        if (GenderList.genderCodeToNameMap.containsKey(mGender))
            mGenderView.setText(GenderList.genderCodeToNameMap.get(mGender));

        if (mVerificationStatus != null) {
            if (mVerificationStatus.equals(Constants.ACCOUNT_VERIFICATION_STATUS_VERIFIED)) {
                mVerificationStatusView.setBackgroundResource(R.drawable.background_verified);
                mVerificationStatusView.setText(R.string.verified);
            } else {
                mVerificationStatusView.setBackgroundResource(R.drawable.background_not_verified);
                mVerificationStatusView.setText(R.string.unverified);
            }
        }
    }

    private void getProfileInfo() {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_USER_INFO)) {
            mUserInformationHolder.setVisibility(View.GONE);
            mUserInfoServiceNotAllowedTextView.setVisibility(View.VISIBLE);
        } else {
            if (mGetProfileInfoTask != null) {
                return;
            }

            setContentShown(false);

            mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
                    Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_INFO_REQUEST, getActivity(), this, false);
            mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void getParentInfo() {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_PARENT)) {
            mParentInformationHolder.setVisibility(View.GONE);
            mParentInfoServiceNotAllowedTextView.setVisibility(View.VISIBLE);
        } else {
            if (mGetParentInfoTask != null) {
                return;
            }
            mGetParentInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PARENT_INFO_REQUEST,
                    Constants.BASE_URL_MM + Constants.URL_GET_PARENT_INFO_REQUEST, getActivity(), this, true);
            mGetParentInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


    }

    private void getOccupationList() {
        if (mGetOccupationTask != null) {
            return;
        }
        mGetOccupationTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_OCCUPATIONS_REQUEST,
                new OccupationRequestBuilder().getGeneratedUri(), getActivity(), this, true);
        mGetOccupationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mGetProfileInfoTask = null;
            setContentShown(true);
            mProgressDialog.dismiss();
            mGetOccupationTask = null;
            return;
        }

        Gson gson = new Gson();

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

            mGetProfileInfoTask = null;
        }

        if (result.getApiCommand().equals(Constants.COMMAND_GET_PARENT_INFO_REQUEST)) {

            try {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mGetParentInfoResponse = gson.fromJson(result.getJsonString(), GetParentInfoResponse.class);

                    if (mGetParentInfoResponse.getFatherName() != null) {
                        mFathersName = mGetParentInfoResponse.getFatherName();
                        mFathersNameView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mFathersNameView.setText(mFathersName);
                    }
                    if (mGetParentInfoResponse.getFatherMobileNumber() != null) {
                        mFathersMobile = mGetParentInfoResponse.getFatherMobileNumber();
                        mFathersMobileView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mFathersMobileView.setText(mFathersMobile);
                    }

                    if (mGetParentInfoResponse.getMotherName() != null) {
                        mMothersName = mGetParentInfoResponse.getMotherName();
                        mMothersNameView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mMothersNameView.setText(mMothersName);
                    }
                    if (mGetParentInfoResponse.getMotherMobileNumber() != null) {
                        mMothersMobile = mGetParentInfoResponse.getMotherMobileNumber();
                        mMothersMobileView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mMothersMobileView.setText(mMothersMobile);
                    }

                } else {
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
            }

            mGetParentInfoTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_OCCUPATIONS_REQUEST)) {

            try {
                mGetOccupationResponse = gson.fromJson(result.getJsonString(), GetOccupationResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mOccupationList = mGetOccupationResponse.getOccupations();
                    if (mGetOccupationResponse.getOccupation(mOccupation) != null) {
                        occupation = mGetOccupationResponse.getOccupation(mOccupation);
                        mOccupationView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mOccupationView.setText(occupation);
                    }
                }
                setContentShown(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mGetOccupationTask = null;
        }
    }

    private void processProfileInfoResponse(String json) {
        Gson gson = new Gson();
        mGetProfileInfoResponse = gson.fromJson(json, GetProfileInfoResponse.class);

        if (mGetProfileInfoResponse.getName() != null)
            mName = mGetProfileInfoResponse.getName();
        if (mGetProfileInfoResponse.getMobileNumber() != null)
            mMobileNumber = mGetProfileInfoResponse.getMobileNumber();

        if (mGetProfileInfoResponse.getDob() != null)
            mDateOfBirth = mGetProfileInfoResponse.getDob();

        if (mGetProfileInfoResponse.getGender() != null) {
            mGender = mGetProfileInfoResponse.getGender();
            mGenderView.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        if (mGetProfileInfoResponse.getSignupTimeFormatted() != null)
            mSignUpTime = mGetProfileInfoResponse.getSignupTimeFormatted();

        mOrganizationName = mGetProfileInfoResponse.getOrganizationName();
        if (mOrganizationName != null && !mOrganizationName.isEmpty())
            mOrganizationNameView.setTextColor(getResources().getColor(R.color.colorPrimary));

        mOccupation = mGetProfileInfoResponse.getOccupation();
        mVerificationStatus = mGetProfileInfoResponse.getVerificationStatus();

        mProfileImageUrl = Utilities.getImage(mGetProfileInfoResponse.getProfilePictures(), Constants.IMAGE_QUALITY_MEDIUM);

        ProfileInfoCacheManager.updateProfileInfoCache(mGetProfileInfoResponse);

        setProfileInformation();
        getOccupationList();
    }
}
