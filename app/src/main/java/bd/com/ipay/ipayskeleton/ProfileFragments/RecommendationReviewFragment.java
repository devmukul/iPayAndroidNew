package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Api.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.IntroductionAndInvite.IntroduceActionResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.RequestMoneyAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.RequestMoneyAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.District;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.DistrictRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.GetDistrictResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.GetThanaResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.Thana;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.ThanaRequestBuilder;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class RecommendationReviewFragment extends Fragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetThanaListAsyncTask = null;
    private GetThanaResponse mGetThanaResponse;

    private HttpRequestGetAsyncTask mGetDistrictListAsyncTask = null;
    private GetDistrictResponse mGetDistrictResponse;

    private HttpRequestPostAsyncTask mRecommendActionTask = null;
    private IntroduceActionResponse mIntroduceActionResponse;


    private HttpRequestPostAsyncTask mAcceptRequestTask = null;

    private HttpRequestPostAsyncTask mCancelRequestTask = null;

    private HttpRequestPostAsyncTask mRejectRequestTask = null;

    private RequestMoneyAcceptRejectOrCancelResponse mRequestMoneyAcceptRejectOrCancelResponse;

    private ProgressDialog mProgressDialog;

    private long mRequestID;
    private String mSenderName;
    private String mSenderMobileNumber;
    private String mPhotoUri;
    private String mFathersName = null;
    private String mMothersname = null;
    private AddressClass mAddress;

    private List<Thana> mThanaList;
    private List<District> mDistrictList;


    private ProfileImageView mProfileImageView;
    private TextView mSenderNameView;
    private TextView mSenderMobileNumberView;
    private TextView mFathersNameView;
    private TextView mMothersNameView;
    private TextView mAddressView;

    private View mFathersNameHolder;
    private View mMothersNameHolder;

    private Button mRejectButton;
    private Button mAcceptButton;
    private Button mSpamButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recommendation_review, container, false);

        getActivity().setTitle(R.string.recommendation_details);

        Bundle bundle = getArguments();

        mRequestID = bundle.getLong(Constants.REQUEST_ID);
        mSenderName = bundle.getString(Constants.NAME);
        mSenderMobileNumber = bundle.getString(Constants.MOBILE_NUMBER);
        mPhotoUri = bundle.getString(Constants.PHOTO_URI);
        mMothersname = bundle.getString(Constants.MOTHERS_NAME);
        mFathersName = bundle.getString(Constants.FATHERS_NAME);
        mAddress = (AddressClass) getArguments().getSerializable(Constants.ADDRESS);

        mProfileImageView = (ProfileImageView) v.findViewById(R.id.profile_picture);
        mSenderNameView = (TextView) v.findViewById(R.id.textview_name);
        mSenderMobileNumberView = (TextView) v.findViewById(R.id.textview_mobile_number);
        mFathersNameView = (TextView) v.findViewById(R.id.textview_fathers_name);
        mMothersNameView = (TextView) v.findViewById(R.id.textview_mothers_name);
        mAddressView = (TextView) v.findViewById(R.id.textview_present_address);

        mFathersNameHolder = v.findViewById(R.id.textview_fathers_name_view);
        mMothersNameHolder = v.findViewById(R.id.textview_mothers_name_view);

        mAcceptButton = (Button) v.findViewById(R.id.button_accept);
        mRejectButton = (Button) v.findViewById(R.id.button_reject);
        mSpamButton = (Button) v.findViewById(R.id.button_spam);

        mProgressDialog = new ProgressDialog(getActivity());

        mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + mPhotoUri, false);

        if (mSenderName == null || mSenderName.isEmpty()) {
            mSenderNameView.setVisibility(View.GONE);
        } else {
            mSenderNameView.setText(mSenderName);
        }

        if (!(mFathersName == null || mFathersName.isEmpty())) {
            mFathersNameView.setText(mFathersName);
        }

        if (!(mMothersname == null || mMothersname.isEmpty())) {
            mMothersNameView.setText(mMothersname);
        }

        mSenderMobileNumberView.setText(mSenderMobileNumber);


        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                attemptSetRecommendationStatus(mRequestID, Constants.INTRODUCTION_REQUEST_ACTION_APPROVE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                attemptSetRecommendationStatus(mRequestID, Constants.INTRODUCTION_REQUEST_ACTION_REJECT);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();

            }
        });

        mSpamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                attemptSetRecommendationStatus(mRequestID, Constants.INTRODUCTION_REQUEST_ACTION_MARK_AS_SPAM);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        if (mAddress != null) {
            getDistrictList();
        }
        return v;
    }

    private void attemptSetRecommendationStatus(long requestID, String recommendationStatus) {
        if (requestID == 0) {
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        mProgressDialog.setMessage(getString(R.string.verifying_user));
        mProgressDialog.show();
        mRecommendActionTask = new HttpRequestPostAsyncTask(Constants.COMMAND_INTRODUCE_ACTION,
                Constants.BASE_URL_MM + Constants.URL_INTRODUCE_ACTION + requestID + "/" + recommendationStatus, null, getActivity());
        mRecommendActionTask.mHttpResponseListener = this;
        mRecommendActionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void getDistrictList() {
        mGetDistrictListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_DISTRICT_LIST,
                new DistrictRequestBuilder().getGeneratedUri(), getActivity(), this);
        mGetDistrictListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getThanaList() {
        mGetThanaListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_THANA_LIST,
                new ThanaRequestBuilder().getGeneratedUri(), getActivity(), this);
        mGetThanaListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadAddresses() {
        if (mAddress == null) {
            mAddressView.setVisibility(View.GONE);
        } else {
            mAddressView.setText(mAddress.toString(mThanaList, mDistrictList));
        }

    }

    @Override
    public void httpResponseReceiver(HttpResponseObject result) {

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mProgressDialog.dismiss();
            mGetThanaListAsyncTask = null;
            mGetDistrictListAsyncTask = null;
            mRecommendActionTask = null;

            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {

            case Constants.COMMAND_INTRODUCE_ACTION:

                try {
                    mIntroduceActionResponse = gson.fromJson(result.getJsonString(), IntroduceActionResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), mIntroduceActionResponse.getMessage(), Toast.LENGTH_LONG).show();

                        getActivity().onBackPressed();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), mIntroduceActionResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
                }

                mProgressDialog.dismiss();
                mRecommendActionTask = null;
                break;
            case Constants.COMMAND_GET_THANA_LIST:
                try {
                    mGetThanaResponse = gson.fromJson(result.getJsonString(), GetThanaResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mThanaList = mGetThanaResponse.getThanas();
                        loadAddresses();

                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.failed_loading_thana_list, Toast.LENGTH_LONG).show();
                            getActivity().onBackPressed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.failed_loading_thana_list, Toast.LENGTH_LONG).show();
                        getActivity().onBackPressed();
                    }
                }

                mGetThanaListAsyncTask = null;
                break;

            case Constants.COMMAND_GET_DISTRICT_LIST:
                try {
                    mGetDistrictResponse = gson.fromJson(result.getJsonString(), GetDistrictResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mDistrictList = mGetDistrictResponse.getDistricts();
                        getThanaList();

                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.failed_loading_district_list, Toast.LENGTH_LONG).show();
                            getActivity().onBackPressed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.failed_loading_district_list, Toast.LENGTH_LONG).show();
                        getActivity().onBackPressed();
                    }
                }

                mGetDistrictListAsyncTask = null;
                break;
        }

    }

}

