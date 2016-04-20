package bd.com.ipay.ipayskeleton.DrawerFragments.HomeFragments.ProfileFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import bd.com.ipay.ipayskeleton.Activities.EditProfileActivity;
import bd.com.ipay.ipayskeleton.Api.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.AddressClass;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.GetIdentificationDocumentResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.GetIntroducerListResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.GetUserAddressResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.IdentificationDocument;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.Introducer;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.UserProfilePictureClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Common.GenderList;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class BasicInfoFragment extends Fragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetProfileInfoResponse mGetProfileInfoResponse;

    private ProgressDialog mProgressDialog;

    private RoundedImageView mProfilePicture;
    private TextView mNameView;
    private TextView mMobileNumberView;
    private TextView mVerificationStatusView;

    private TextView mEmailAddressView;
    private TextView mDateOfBirthView;

    private TextView mFathersNameView;
    private TextView mMothersNameView;
    private TextView mSpouseNameView;

    private TextView mFathersMobileNumberView;
    private TextView mMothersMobileNumberView;
    private TextView mSpouseMobileNumberView;

    private TextView mOccupationView;
    private TextView mGenderView;

    private SharedPreferences pref;

    private String mName = "";
    private String mMobileNumber = "";
    private Set<UserProfilePictureClass> mProfilePictures;

    private String mEmailAddress = "";
    private String mDateOfBirth = "";

    private String mFathersName = "";
    private String mMothersName = "";
    private String mSpouseName = "";

    private String mFathersMobileNumber = "";
    private String mMothersMobileNumber = "";
    private String mSpouseMobileNumber = "";

    private int mOccupation = 0;
    private String mGender = "";
    private String mVerificationStatus = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu.findItem(R.id.action_search_contacts) != null)
            menu.findItem(R.id.action_search_contacts).setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_basic_info, container, false);
        pref = getActivity().getSharedPreferences(Constants.ApplicationTag, Activity.MODE_PRIVATE);
        getActivity().setTitle(R.string.profile);

        mProfilePictures = new HashSet<>();

        mProfilePicture = (RoundedImageView) v.findViewById(R.id.profile_picture);
        mNameView = (TextView) v.findViewById(R.id.textview_name);
        mMobileNumberView = (TextView) v.findViewById(R.id.textview_mobile_number);
        mVerificationStatusView = (TextView) v.findViewById(R.id.textview_verification_status);

        mEmailAddressView = (TextView) v.findViewById(R.id.textview_email);
        mDateOfBirthView = (TextView) v.findViewById(R.id.textview_date_of_birth);

        mFathersNameView = (TextView) v.findViewById(R.id.textview_fathers_name);
        mMothersNameView = (TextView) v.findViewById(R.id.textview_mothers_name);
        mSpouseNameView = (TextView) v.findViewById(R.id.textview_spouse_name);

        mFathersMobileNumberView = (TextView) v.findViewById(R.id.textview_fathers_mobile_number);
        mMothersMobileNumberView = (TextView) v.findViewById(R.id.textview_mothers_mobile_number);
        mSpouseMobileNumberView = (TextView) v.findViewById(R.id.textview_spouse_mobile_number);

        mOccupationView = (TextView) v.findViewById(R.id.textview_occupation);
        mGenderView = (TextView) v.findViewById(R.id.textview_gender);

        mMobileNumber = pref.getString(Constants.USERID, "");
        mGender = pref.getString(Constants.GENDER, "");
        mDateOfBirth = pref.getString(Constants.BIRTHDAY, "");

        mProgressDialog = new ProgressDialog(getActivity());

        setProfilePicture("");
        getProfileInfo();

        return v;
    }

    private void setProfileInformation() {
        if (mProfilePictures.size() > 0) {

            String imageUrl = "";
            for (Iterator<UserProfilePictureClass> it = mProfilePictures.iterator(); it.hasNext(); ) {
                UserProfilePictureClass userProfilePictureClass = it.next();
                imageUrl = userProfilePictureClass.getUrl();
                break;
            }
            setProfilePicture(imageUrl);
        }
        mMobileNumberView.setText(mMobileNumber);
        mNameView.setText(mName);

        mEmailAddressView.setText(mEmailAddress);
        mGenderView.setText(mGender);
        mDateOfBirthView.setText(mDateOfBirth);

        mFathersNameView.setText(mFathersName);
        mMothersNameView.setText(mMothersName);
        mSpouseNameView.setText(mSpouseName);

        mFathersMobileNumberView.setText(mFathersMobileNumber);
        mMothersMobileNumberView.setText(mMothersMobileNumber);
        mSpouseMobileNumberView.setText(mSpouseMobileNumber);

        if (mOccupation == 0) mOccupationView.setText("");
        else {
            String[] occupationArray = getResources().getStringArray(R.array.occupations);
            mOccupationView.setText(occupationArray[mOccupation]);
        }

        if (GenderList.genderCodeToNameMap.containsKey(mGender))
            mGenderView.setText(GenderList.genderCodeToNameMap.get(mGender));

        if (mVerificationStatus != null) {
            if (mVerificationStatus.equals(Constants.VERIFICATION_STATUS_VERIFIED)) {
                mVerificationStatusView.setBackgroundResource(R.drawable.background_verified);
                mVerificationStatusView.setText(R.string.verified);
            } else {
                mVerificationStatusView.setBackgroundResource(R.drawable.background_not_verified);
                mVerificationStatusView.setText(R.string.not_verified);
            }
        }
    }

    public void editProfile(int targetTab) {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        intent.putExtra(EditProfileActivity.TARGET_TAB, targetTab);
        startActivity(intent);
    }

    private void getProfileInfo() {
        if (mGetProfileInfoTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.fetching_profile_information));
        mProgressDialog.show();

        mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
                Constants.BASE_URL + "/" + Constants.URL_GET_PROFILE_INFO_REQUEST, getActivity(), this);
        mGetProfileInfoTask.execute();
    }
    private void setProfilePicture(String url) {
        try {
            if (!url.equals("")) {
                if (!url.startsWith("content:"))
                    url = Constants.BASE_URL_IMAGE_SERVER + url;

                Glide.with(getActivity())
                        .load(url)
                        .crossFade()
                        .error(R.drawable.ic_person)
                        .transform(new CircleTransform(getActivity()))
                        .into(mProfilePicture);
            } else {
                Glide.with(getActivity())
                        .load(R.drawable.ic_person)
                        .crossFade()
                        .into(mProfilePicture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void httpResponseReceiver(String result) {
        if (result == null) {
            mProgressDialog.dismiss();
            mGetProfileInfoTask = null;
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.request_failed, Toast.LENGTH_SHORT).show();
            return;
        }


        List<String> resultList = Arrays.asList(result.split(";"));
        Gson gson = new Gson();

        if (resultList.get(0).equals(Constants.COMMAND_GET_PROFILE_INFO_REQUEST)) {

            try {
                mGetProfileInfoResponse = gson.fromJson(resultList.get(2), GetProfileInfoResponse.class);
                if (resultList.get(1) != null && resultList.get(1).equals(Constants.HTTP_RESPONSE_STATUS_OK)) {
                    if (mGetProfileInfoResponse.getName() != null)
                        mName = mGetProfileInfoResponse.getName();
                    if (mGetProfileInfoResponse.getMobileNumber() != null)
                        mMobileNumber = mGetProfileInfoResponse.getMobileNumber();
                    if (mGetProfileInfoResponse.getEmail() != null)
                        mEmailAddress = mGetProfileInfoResponse.getEmail();
                    if (mGetProfileInfoResponse.getDateOfBirth() != null)
                        mDateOfBirth = mGetProfileInfoResponse.getDateOfBirth();

                    if (mGetProfileInfoResponse.getFather() != null)
                        mFathersName = mGetProfileInfoResponse.getFather();
                    if (mGetProfileInfoResponse.getMother() != null)
                        mMothersName = mGetProfileInfoResponse.getMother();
                    if (mGetProfileInfoResponse.getSpouse() != null)
                        mSpouseName = mGetProfileInfoResponse.getSpouse();

                    if (mGetProfileInfoResponse.getFatherMobileNumber() != null)
                        mFathersMobileNumber = mGetProfileInfoResponse.getFatherMobileNumber();
                    if (mGetProfileInfoResponse.getMotherMobileNumber() != null)
                        mMothersMobileNumber = mGetProfileInfoResponse.getMotherMobileNumber();
                    if (mGetProfileInfoResponse.getSpouseMobileNumber() != null)
                        mSpouseMobileNumber = mGetProfileInfoResponse.getSpouseMobileNumber();

                    if (mGetProfileInfoResponse.getGender() != null)
                        mGender = mGetProfileInfoResponse.getGender();

                    mOccupation = mGetProfileInfoResponse.getOccupation();
                    mVerificationStatus = mGetProfileInfoResponse.getVerificationStatus();
                    mProfilePictures = mGetProfileInfoResponse.getProfilePictures();

                    setProfileInformation();
                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT).show();
            }

            mGetProfileInfoTask = null;
            mProgressDialog.dismiss();
        }
    }
}