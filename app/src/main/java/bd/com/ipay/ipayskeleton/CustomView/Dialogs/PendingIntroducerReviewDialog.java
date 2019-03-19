package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.GetPendingIntroducerRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.PendingIntroducer;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.IntroduceActionResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class PendingIntroducerReviewDialog extends MaterialDialog.Builder implements HttpResponseListener {

    private HttpRequestPostAsyncTask mPendingIntroducerActionTask = null;
    private IntroduceActionResponse mPendingIntroducerActionResponse;

    private MaterialDialog reviewDialog;
    private CustomProgressDialog mProgressDialog;

    private Context Context;

    private PendingIntroducer mPendingIntroducer;

    private long mRequestID;
    private String mPendingIntroducerName;
    private String mPendingIntroucerMobileNumber;
    private String mPendingIntroducerPhotoUri;

    private ProfileImageView mPendingIntroducerProfileImageView;
    private TextView mPendingIntroducerNameView;
    private TextView mPendingIntroducerMobileNumberView;

    private Button mRejectButton;
    private Button mAcceptButton;

    private ActionCheckerListener mActionCheckerListener;

    public PendingIntroducerReviewDialog(Context context, PendingIntroducer pendingIntroducer) {
        super(context);

        this.Context = context;
        this.mPendingIntroducer = pendingIntroducer;
        initializeView();
    }

    public void initializeView() {

        reviewDialog = new MaterialDialog.Builder(this.getContext())
                .title(R.string.request_to_introduce)
                .customView(R.layout.dialog_pending_introducer_review, true)
                .show();

        View v = reviewDialog.getCustomView();
        mPendingIntroducerProfileImageView = (ProfileImageView) v.findViewById(R.id.profile_picture);
        mPendingIntroducerNameView = (TextView) v.findViewById(R.id.textview_name);
        mPendingIntroducerMobileNumberView = (TextView) v.findViewById(R.id.textview_mobile_number);

        mRequestID = mPendingIntroducer.getId();
        mPendingIntroducerName = mPendingIntroducer.getName();
        mPendingIntroucerMobileNumber = mPendingIntroducer.getMobileNumber();
        mPendingIntroducerPhotoUri = mPendingIntroducer.getImageUrl();

        mAcceptButton = (Button) v.findViewById(R.id.button_accept);
        mRejectButton = (Button) v.findViewById(R.id.button_reject);

        mProgressDialog = new CustomProgressDialog(Context);

        setPendingIntroducerUserInfo();

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewDialog.dismiss();
                attemptAcceptRejectPendingIntroducer(mRequestID, Constants.INTRODUCTION_REQUEST_ACTION_APPROVE);
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewDialog.dismiss();
                attemptAcceptRejectPendingIntroducer(mRequestID, Constants.INTRODUCTION_REQUEST_ACTION_REJECT);


            }
        });

    }

    private void setPendingIntroducerUserInfo() {
        if (mPendingIntroducerName == null || mPendingIntroducerName.isEmpty()) {
            mPendingIntroducerNameView.setVisibility(View.GONE);
        } else {
            mPendingIntroducerNameView.setText(mPendingIntroducerName);
        }

        mPendingIntroducerProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + mPendingIntroducerPhotoUri, false);
        mPendingIntroducerMobileNumberView.setText(mPendingIntroucerMobileNumber);
    }

    public void setActionCheckerListener(ActionCheckerListener actionCheckerListener) {
        mActionCheckerListener = actionCheckerListener;
    }

    private void attemptAcceptRejectPendingIntroducer(long requestID, String introducerAcceptRejectStatus) {

        mProgressDialog.show();
        GetPendingIntroducerRequestBuilder getSecurityQuestionBuilder = new GetPendingIntroducerRequestBuilder(requestID, introducerAcceptRejectStatus);
        String url = getSecurityQuestionBuilder.getGeneratedUri();

        mPendingIntroducerActionTask = new HttpRequestPostAsyncTask(Constants.COMMAND_INTRODUCE_ACTION, url, null, Context, false);
        mPendingIntroducerActionTask.mHttpResponseListener = this;
        mPendingIntroducerActionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mPendingIntroducerActionTask = null;
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {

            case Constants.COMMAND_INTRODUCE_ACTION:
                try {
                    mPendingIntroducerActionResponse = gson.fromJson(result.getJsonString(), IntroduceActionResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (Context != null)
                            Toaster.makeText(Context, mPendingIntroducerActionResponse.getMessage(), Toast.LENGTH_LONG);

                        if (mActionCheckerListener != null) {
                            mActionCheckerListener.ifFinishNeeded();
                        }
                    } else {
                        if (Context != null)
                            Toaster.makeText(Context, mPendingIntroducerActionResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (Context != null)
                        Toaster.makeText(Context, R.string.service_not_available, Toast.LENGTH_LONG);
                }

                mProgressDialog.dismiss();
                mPendingIntroducerActionTask = null;
                break;
        }
    }

    public interface ActionCheckerListener {
        void ifFinishNeeded();
    }
}

