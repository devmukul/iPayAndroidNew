package bd.com.ipay.ipayskeleton.PaymentFragments.CommonFragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.InviteDialog;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.BasicInfo.GetUserInfoRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.BasicInfo.GetUserInfoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class InviteToiPayFragment extends ProgressFragment implements HttpResponseListener {
    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetUserInfoResponse mGetUserInfoResponse;

    private TextView mMobileNumberView;
    private Button mInviteToIpayButton;

    private View mInviteContainer;
    private View mUnverifiedContainer;

    private String mMobileNumber;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_invite_to_ipay, container, false);

        mMobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);

        mMobileNumberView = (TextView) v.findViewById(R.id.textview_mobile_number);
        mInviteToIpayButton = (Button) v.findViewById(R.id.button_invite_to_ipay);

        mInviteContainer = v.findViewById(R.id.invite_container);
        mUnverifiedContainer = v.findViewById(R.id.unverified_container);

        mMobileNumberView.setText(mMobileNumber);
        mInviteToIpayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new InviteDialog(getContext(), mMobileNumber).setFinishCheckerListener(new InviteDialog.FinishCheckerListener() {
                    @Override
                    public void ifFinishNeeded() {
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }
                });
            }
        });

        getProfileInfo(ProfileInfoCacheManager.getMobileNumber());

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
    }

    private void getProfileInfo(String mobileNumber) {
        if (mGetProfileInfoTask != null) {
            return;
        }

        GetUserInfoRequestBuilder mGetUserInfoRequestBuilder = new GetUserInfoRequestBuilder(mobileNumber);

        String mUri = mGetUserInfoRequestBuilder.getGeneratedUri();
        mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_USER_INFO,
                mUri, getActivity(), this);

        mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void httpResponseReceiver(HttpResponseObject result) {
        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.failed_request, Toast.LENGTH_SHORT).show();

            return;
        }
        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_USER_INFO)) {

            try {
                setContentShown(true);
                mGetUserInfoResponse = gson.fromJson(result.getJsonString(), GetUserInfoResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (mGetUserInfoResponse.getAccountStatus().equals(Constants.ACCOUNT_VERIFICATION_STATUS_VERIFIED)) {
                        mInviteContainer.setVisibility(View.VISIBLE);
                    } else {
                        mUnverifiedContainer.setVisibility(View.VISIBLE);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mGetProfileInfoTask = null;
        }
    }
}