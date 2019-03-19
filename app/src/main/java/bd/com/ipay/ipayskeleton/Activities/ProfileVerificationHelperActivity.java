package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutResponse;
import bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments.OnBoardAddBasicInfoFragment;
import bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments.OnBoardAddBasicInfoHelperFragment;
import bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments.OnBoardAddSourceOfFundHelperFragment;
import bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments.OnBoardContactsFragment;
import bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments.OnBoardIdentificationDocumentUploadFragment;
import bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments.OnBoardPhotoIdUploadHelperFragment;
import bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments.OnBoardProfilePictureUploadHelperFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationDocumentFragments.PreviewIdentificationDocumentFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeepLinkAction;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ProfileVerificationHelperActivity extends BaseActivity implements HttpResponseListener {
    private HttpRequestPostAsyncTask mLogoutTask = null;
    private LogoutResponse mLogOutResponse;
    private CustomProgressDialog mProgressDialog;
    public Uri mProfilePhotoUri;
    private DeepLinkAction mDeepLinkAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_verification_helper);
        mDeepLinkAction = getIntent().getParcelableExtra(Constants.DEEP_LINK_ACTION);
        SharedPrefManager.setFirstLaunch(false);
        mProgressDialog = new CustomProgressDialog(ProfileVerificationHelperActivity.this);
        if (ProfileInfoCacheManager.isSwitchedFromSignup()) {
            switchToProfilePictureFragment();
        } else {
            if (!ProfileInfoCacheManager.isProfilePictureUploaded()) {
                switchToProfilePictureFragment();
            } else if (!ProfileInfoCacheManager.isIdentificationDocumentUploaded()) {
                switchToPhotoIdUploadHelperFragment();
            } else if (!ProfileInfoCacheManager.isBasicInfoAdded()) {
                switchToBasicInfoEditHelperFragment();
            } else if (!ProfileInfoCacheManager.isSourceOfFundAdded()) {
                switchToSourceOfFundHelperFragment();
            } else {
                switchToHomeActivity();
            }
        }
    }

    public void switchToSourceOfFundHelperFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 4)
            getSupportFragmentManager().popBackStackImmediate();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new OnBoardAddSourceOfFundHelperFragment()).addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            new AlertDialog.Builder(ProfileVerificationHelperActivity.this)
                    .setMessage(R.string.are_you_sure_to_exit)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (SharedPrefManager.isRememberMeActive()) {
                                finish();
                            } else {
                                if (Utilities.isConnectionAvailable(ProfileVerificationHelperActivity.this)) {
                                    attemptLogout();
                                } else {
                                    ProfileInfoCacheManager.setLoggedInStatus(false);
                                    ((MyApplication) ProfileVerificationHelperActivity.this.getApplication()).clearTokenAndTimer();
                                    finish();
                                }
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }
    }

    private void attemptLogout() {
        if (mLogoutTask != null) {
            return;
        }

        mProgressDialog.show();
        LogoutRequest mLogoutModel = new LogoutRequest(ProfileInfoCacheManager.getMobileNumber());
        Gson gson = new Gson();
        String json = gson.toJson(mLogoutModel);

        mLogoutTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LOG_OUT,
                Constants.BASE_URL_MM + Constants.URL_LOG_OUT, json, ProfileVerificationHelperActivity.this, false);
        mLogoutTask.mHttpResponseListener = this;
        mLogoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void switchToHomeActivity() {
        if (mDeepLinkAction != null)
            Utilities.performDeepLinkAction(this, mDeepLinkAction);
        else {
            Utilities.hideKeyboard(this);
            Intent intent = new Intent(ProfileVerificationHelperActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void switchToProfilePictureFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OnBoardProfilePictureUploadHelperFragment()).addToBackStack(null).commit();
    }

    public void switchToPhotoIdUploadHelperFragment() {
        OnBoardPhotoIdUploadHelperFragment onBoardIcdentificationFragment = new OnBoardPhotoIdUploadHelperFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, onBoardIcdentificationFragment).addToBackStack(null).commit();
    }

    public void switchToUploadIdentificationDocumentFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 4)
            getSupportFragmentManager().popBackStackImmediate();
        OnBoardIdentificationDocumentUploadFragment uploadIdentificationFragment = new OnBoardIdentificationDocumentUploadFragment();
        uploadIdentificationFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, uploadIdentificationFragment).addToBackStack(null).commit();
    }

    public void switchToPreviewIdentificationDocumentFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 3)
            getSupportFragmentManager().popBackStackImmediate();
        PreviewIdentificationDocumentFragment previewIdentificationDocumentFragment = new PreviewIdentificationDocumentFragment();
        previewIdentificationDocumentFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, previewIdentificationDocumentFragment).addToBackStack(null).commit();
    }

    public void switchToBasicInfoEditHelperFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OnBoardAddBasicInfoHelperFragment()).addToBackStack(null).commit();
    }

    public void switchToBasicInfoEditFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OnBoardAddBasicInfoFragment()).addToBackStack(null).commit();
    }

    public void switchToContactFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OnBoardContactsFragment()).commit();
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, this, mProgressDialog)) {
            mProgressDialog.dismiss();
            mLogoutTask = null;
            return;
        }

        Gson gson = new Gson();
        switch (result.getApiCommand()) {
            case Constants.COMMAND_LOG_OUT:
                try {
                    mLogOutResponse = gson.fromJson(result.getJsonString(), LogoutResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        ((MyApplication) this.getApplication()).clearTokenAndTimer();
                        finish();
                    } else {
                        Toast.makeText(ProfileVerificationHelperActivity.this, mLogOutResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileVerificationHelperActivity.this, R.string.could_not_sign_out, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected Context setContext() {
        return ProfileVerificationHelperActivity.this;
    }
}
