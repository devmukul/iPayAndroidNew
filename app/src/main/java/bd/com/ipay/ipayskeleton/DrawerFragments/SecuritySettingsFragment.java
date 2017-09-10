package bd.com.ipay.ipayskeleton.DrawerFragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragmentV4;
import bd.com.ipay.ipayskeleton.CustomView.IconifiedTextViewWithButton;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.FingerPrintAuthenticationManager.FingerPrintAuthenticationManager;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SecuritySettingsFragment extends BaseFragmentV4 implements HttpResponseListener {

    private HttpRequestPostAsyncTask mLogoutTask = null;
    private LogoutResponse mLogOutResponse;

    private IconifiedTextViewWithButton mSetPINHeader;
    private IconifiedTextViewWithButton mChangePasswordHeader;
    private IconifiedTextViewWithButton mTrustedDevicesHeader;
    private IconifiedTextViewWithButton mPasswordRecoveryHeader;
    private IconifiedTextViewWithButton mLogoutHeader;
    private IconifiedTextViewWithButton mFingerprintOptionHeader;
    private IconifiedTextViewWithButton mTwoFAHeader;
    private View mFingerprintOptionHolder;

    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        setTitle();

        mSetPINHeader = (IconifiedTextViewWithButton) view.findViewById(R.id.set_pin);
        mChangePasswordHeader = (IconifiedTextViewWithButton) view.findViewById(R.id.change_password);
        mTrustedDevicesHeader = (IconifiedTextViewWithButton) view.findViewById(R.id.trusted_device);
        mPasswordRecoveryHeader = (IconifiedTextViewWithButton) view.findViewById(R.id.password_recovery);
        mLogoutHeader = (IconifiedTextViewWithButton) view.findViewById(R.id.logout_from_all_devices);
        mFingerprintOptionHeader = (IconifiedTextViewWithButton) view.findViewById(R.id.login_with_fingerprint);
        mTwoFAHeader = (IconifiedTextViewWithButton) view.findViewById(R.id.implement_2fa);
        mFingerprintOptionHolder = view.findViewById(R.id.fingerprint_layout);

        mProgressDialog = new ProgressDialog(getActivity());

        setVisibilityOfFingerPrintOption();
        setButtonActions();

        if (getArguments() != null && getArguments().getBoolean(Constants.EXPAND_PIN, false)) {
            mSetPINHeader.performClick();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_security_settings));
    }


    private void setVisibilityOfFingerPrintOption() {
        FingerPrintAuthenticationManager fingerprintAuthenticationManager = new FingerPrintAuthenticationManager(getActivity());
        if (fingerprintAuthenticationManager.ifFingerprintAuthenticationSupported())
            mFingerprintOptionHolder.setVisibility(View.VISIBLE);
        else
            mFingerprintOptionHolder.setVisibility(View.GONE);
    }

    private void setButtonActions() {
        mSetPINHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.CHANGE_PIN)
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchToSetPinFragment();
            }
        });

        mChangePasswordHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.CHANGE_PASSWORD)
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchToChangePasswordFragment();
            }
        });

        mTrustedDevicesHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.SEE_TRUSTED_DEVICES)
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchToTrustedDeviceFragment();
            }
        });

        mPasswordRecoveryHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchToPasswordRecoveryFragment();
            }
        });

        mLogoutHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.SIGN_OUT_FROM_ALL_DEVICES)
            public void onClick(View v) {
                showLogoutFromAllDevicesDialog();
            }
        });

        mFingerprintOptionHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchToFingerprintAuthenticationSettingsFragment();
            }
        });
        mTwoFAHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchTo2FaSettingsFragment();
            }
        });
    }

    private void showLogoutFromAllDevicesDialog() {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(getActivity());
        dialog
                .content(R.string.logout_from_all_device_warning)
                .cancelable(false)
                .positiveText(R.string.yes)
                .negativeText(R.string.no);

        dialog.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                logOutFromAllDevices();
            }
        });

        dialog.show();
    }

    private void logOutFromAllDevices() {
        if (mLogoutTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_signing_out));
        mProgressDialog.show();

        LogoutRequest mLogoutModel = new LogoutRequest(ProfileInfoCacheManager.getMobileNumber());
        Gson gson = new Gson();
        String json = gson.toJson(mLogoutModel);

        mLogoutTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LOG_OUT,
                Constants.BASE_URL_MM + Constants.URL_LOG_OUT_from_all_device, json, getActivity());
        mLogoutTask.mHttpResponseListener = this;

        mLogoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setTitle() {
        getActivity().setTitle(R.string.security);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mProgressDialog.dismiss();
            mLogoutTask = null;
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_LOG_OUT)) {

            try {
                mLogOutResponse = gson.fromJson(result.getJsonString(), LogoutResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    ((MyApplication) getActivity().getApplication()).launchLoginPage(null);
                } else {
                    Toast.makeText(getActivity(), mLogOutResponse.getMessage(), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.could_not_sign_out, Toast.LENGTH_LONG).show();
            }

            mProgressDialog.dismiss();
            mLogoutTask = null;

        }
    }

}