package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.DeviceTrustFragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.DeviceTrustActivity;
import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestDeleteAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.BulkSignUp.GetUserDetailsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionStatusResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.GetTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.RemoveTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.TrustedDevice;
import bd.com.ipay.ipayskeleton.Model.GetCardResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class RemoveTrustedDeviceFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetTrustedDeviceTask = null;
    private GetTrustedDeviceResponse mGetTrustedDeviceResponse = null;

    private HttpRequestDeleteAsyncTask mRemoveTrustedDeviceTask = null;
    private RemoveTrustedDeviceResponse mRemoveTrustedDeviceResponse = null;

    private HttpRequestPostAsyncTask mAddTrustedDeviceTask = null;
    private AddToTrustedDeviceResponse mAddToTrustedDeviceResponse;

    private HttpRequestGetAsyncTask mGetProfileCompletionStatusTask = null;
    private ProfileCompletionStatusResponse mProfileCompletionStatusResponse;

    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetProfileInfoResponse mGetProfileInfoResponse;

    private HttpRequestGetAsyncTask mGetAllAddedCards = null;
    private GetCardResponse mGetCardResponse;

    private HttpRequestPostAsyncTask mLogoutTask = null;
    private LogoutResponse mLogOutResponse;

    private HttpRequestGetAsyncTask mGetBulkSignupUserDetailsTask = null;
    private GetUserDetailsResponse mGetUserDetailsResponse;

    private ArrayList<TrustedDevice> mTrustedDeviceList;
    private TrustedDeviceAdapter mTrustedDeviceAdapter;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mTrustedDevicesRecyclerView;

    private CustomProgressDialog mProgressDialog;
    private Button mLogOutButton;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_remove_trusted_device));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_remove_trusted_device, container, false);
        setTitle();

        mTrustedDevicesRecyclerView = (RecyclerView) v.findViewById(R.id.list_trusted_devices);
        mProgressDialog = new CustomProgressDialog(getActivity());
        mLogOutButton = (Button) v.findViewById(R.id.button_logout);

        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogout();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setContentShown(false);

        getTrustedDeviceList();
    }

    private void showDeviceRemoveConfirmationDialog(final long id) {
        MaterialDialog.Builder mRemoveConfirmationDialogBuilder;
        MaterialDialog mRemoveConfirmationDialog;
        mRemoveConfirmationDialogBuilder = new MaterialDialog.Builder(getActivity())
                .content(getString(R.string.confirmation_remove_trusted_device))
                .cancelable(true)
                .positiveText(R.string.remove)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeTrustedDevice(id);
                    }
                });

        mRemoveConfirmationDialog = mRemoveConfirmationDialogBuilder.build();
        mRemoveConfirmationDialog.show();
    }

    private void getTrustedDeviceList() {
        if (mGetTrustedDeviceTask != null) {
            return;
        }

        mGetTrustedDeviceTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRUSTED_DEVICES,
                Constants.BASE_URL_MM + Constants.URL_GET_TRUSTED_DEVICES, getActivity(), this, true);
        mGetTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getAddedCards() {
        if (mGetAllAddedCards != null) return;
        else {
            mGetAllAddedCards = new HttpRequestGetAsyncTask(Constants.COMMAND_ADD_CARD,
                    Constants.BASE_URL_MM + Constants.URL_GET_CARD, getActivity(), this, true);
            mGetAllAddedCards.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void removeTrustedDevice(long id) {
        if (mRemoveTrustedDeviceTask != null)
            return;

        mProgressDialog.show();

        mRemoveTrustedDeviceTask = new HttpRequestDeleteAsyncTask(Constants.COMMAND_REMOVE_TRUSTED_DEVICE,
                Constants.BASE_URL_MM + Constants.URL_REMOVE_TRUSTED_DEVICE + id, getActivity(), this, false);
        mRemoveTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setTitle() {
        getActivity().setTitle(R.string.browsers_and_apps);
    }

    private void attemptTrustedDeviceAdd() {
        if (mAddTrustedDeviceTask != null) {
            return;
        }

        mProgressDialog.show();

        String mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());
        String mDeviceName = DeviceInfoFactory.getDeviceName();

        AddToTrustedDeviceRequest mAddToTrustedDeviceRequest = new AddToTrustedDeviceRequest(mDeviceName,
                Constants.MOBILE_ANDROID + mDeviceID, null);
        Gson gson = new Gson();
        String json = gson.toJson(mAddToTrustedDeviceRequest);
        mAddTrustedDeviceTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_TRUSTED_DEVICE,
                Constants.BASE_URL_MM + Constants.URL_ADD_TRUSTED_DEVICE, json, getActivity(), false);
        mAddTrustedDeviceTask.mHttpResponseListener = this;
        mAddTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                Constants.BASE_URL_MM + Constants.URL_LOG_OUT, json, getActivity(), false);
        mLogoutTask.mHttpResponseListener = this;

        mLogoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void navigateToDesiredActivityViaWallet() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.putExtra(Constants.TRANSACTION_DETAILS, ((DeviceTrustActivity) getActivity()).transactionHistory);
        intent.putExtra(Constants.ACTION_FROM_NOTIFICATION, ((DeviceTrustActivity) getActivity()).isAccepted);
        intent.putExtra(Constants.DESIRED_ACTIVITY, ((DeviceTrustActivity) getActivity()).desiredActivity);
        startActivity(intent);
    }

    private void getProfileCompletionStatus() {

        mProgressDialog.show();

        if (mGetProfileCompletionStatusTask != null) {
            return;
        }

        mGetProfileCompletionStatusTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_COMPLETION_STATUS, getActivity(), this, true);
        mGetProfileCompletionStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getProfileInfo() {
        if (mGetProfileInfoTask != null) {
            return;
        }

        mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_INFO_REQUEST, getActivity(), true);
        mGetProfileInfoTask.mHttpResponseListener = this;
        mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog) && !result.getApiCommand().equals(Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS)) {
            mProgressDialog.dismiss();
            mGetTrustedDeviceTask = null;
            mAddTrustedDeviceTask = null;
            mLogoutTask = null;
            mRemoveTrustedDeviceTask = null;
            mGetAllAddedCards = null;
            mGetProfileCompletionStatusTask = null;
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_TRUSTED_DEVICES:
                try {
                    mGetTrustedDeviceResponse = gson.fromJson(result.getJsonString(), GetTrustedDeviceResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        processTrustedDeviceList(result.getJsonString());
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), mGetTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mGetTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                mProgressDialog.dismiss();
                mGetTrustedDeviceTask = null;
                break;

            case Constants.COMMAND_REMOVE_TRUSTED_DEVICE:
                try {
                    mRemoveTrustedDeviceResponse = gson.fromJson(result.getJsonString(), RemoveTrustedDeviceResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.success_device_removed, Toast.LENGTH_LONG).show();
                        }
                        mProgressDialog.show();
                        // Add the device as trusted immediately after removing any device
                        getTrustedDeviceList();
                        attemptTrustedDeviceAdd();
                    } else {
                        mProgressDialog.dismiss();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), mRemoveTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mRemoveTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                mRemoveTrustedDeviceTask = null;
                break;

            case Constants.COMMAND_ADD_TRUSTED_DEVICE:
                try {
                    mAddToTrustedDeviceResponse = gson.fromJson(result.getJsonString(), AddToTrustedDeviceResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        String UUID = mAddToTrustedDeviceResponse.getUUID();
                        ProfileInfoCacheManager.setUUID(UUID);
                        getProfileInfo();
                    } else {
                        getTrustedDeviceList();
                        Toast.makeText(getActivity(), mAddToTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.failed_add_trusted_device, Toast.LENGTH_LONG).show();
                }
                mAddTrustedDeviceTask = null;
                break;

            case Constants.COMMAND_LOG_OUT:
                try {
                    mLogOutResponse = gson.fromJson(result.getJsonString(), LogoutResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK)
                        ((MyApplication) getActivity().getApplication()).launchLoginPage(null);
                    else
                        Toast.makeText(getActivity(), mLogOutResponse.getMessage(), Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.could_not_sign_out, Toast.LENGTH_LONG).show();
                }

                mProgressDialog.dismiss();
                mLogoutTask = null;
                break;

            case Constants.COMMAND_GET_PROFILE_INFO_REQUEST:
                try {
                    mGetProfileInfoResponse = gson.fromJson(result.getJsonString(), GetProfileInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        ProfileInfoCacheManager.updateProfileInfoCache(mGetProfileInfoResponse);
                        ProfileInfoCacheManager.saveMainUserProfileInfo(Utilities.getMainUserProfileInfoString(mGetProfileInfoResponse));
                        getBulkSignUpUserDetails();

                    } else {
                        mProgressDialog.dismiss();
                        Toaster.makeText(getActivity(), R.string.profile_info_get_failed, Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();
                    Toaster.makeText(getActivity(), R.string.profile_info_get_failed, Toast.LENGTH_SHORT);
                }

                mGetProfileInfoTask = null;
                break;

            case Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mGetUserDetailsResponse = gson.fromJson(result.getJsonString(), GetUserDetailsResponse.class);
                        BulkSignupUserDetailsCacheManager.updateBulkSignupUserInfoCache(mGetUserDetailsResponse);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                getProfileCompletionStatus();
                mGetBulkSignupUserDetailsTask = null;
                break;

            case Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS:
                try {
                    mProfileCompletionStatusResponse = gson.fromJson(result.getJsonString(), ProfileCompletionStatusResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mProfileCompletionStatusResponse.initScoreFromPropertyName();
                        ProfileInfoCacheManager.switchedFromSignup(false);
                        ProfileInfoCacheManager.uploadProfilePicture(mProfileCompletionStatusResponse.isPhotoUpdated());
                        ProfileInfoCacheManager.uploadIdentificationDocument(mProfileCompletionStatusResponse.isPhotoIdUpdated());
                        ProfileInfoCacheManager.addBasicInfo(mProfileCompletionStatusResponse.isOnboardBasicInfoUpdated());
                        ProfileInfoCacheManager.addSourceOfFund(mProfileCompletionStatusResponse.isBankAdded());

                        if (ProfileInfoCacheManager.isSourceOfFundAdded()) {
                            if (ProfileInfoCacheManager.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE && !ProfileInfoCacheManager.isAccountVerified() && (!ProfileInfoCacheManager.isProfilePictureUploaded() || !ProfileInfoCacheManager.isIdentificationDocumentUploaded()
                                    || !ProfileInfoCacheManager.isBasicInfoAdded() || !ProfileInfoCacheManager.isSourceOfFundAdded())) {
                                ((DeviceTrustActivity) getActivity()).switchToProfileCompletionHelperActivity();
                            } else {
                                if (((DeviceTrustActivity) getActivity()).transactionHistory != null) {
                                    navigateToDesiredActivityViaWallet();
                                } else {
                                    ((DeviceTrustActivity) getActivity()).switchToHomeActivity();
                                }
                            }
                        } else getAddedCards();
                    } else {
                        if (getActivity() != null) {
                            if (((DeviceTrustActivity) getActivity()).transactionHistory != null) {
                                navigateToDesiredActivityViaWallet();
                            } else {
                                ((DeviceTrustActivity) getActivity()).switchToHomeActivity();
                            }
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        if (((DeviceTrustActivity) getActivity()).transactionHistory != null) {
                            navigateToDesiredActivityViaWallet();
                        } else {
                            ((DeviceTrustActivity) getActivity()).switchToHomeActivity();
                        }
                    }
                }
                mProgressDialog.dismiss();
                mGetProfileCompletionStatusTask = null;
                break;

            case Constants.COMMAND_ADD_CARD:
                try {
                    mGetCardResponse = gson.fromJson(result.getJsonString(), GetCardResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                        if (!mGetCardResponse.isAnyCardVerified()) {
                            ProfileInfoCacheManager.addSourceOfFund(false);
                        } else ProfileInfoCacheManager.addSourceOfFund(true);

                        if (ProfileInfoCacheManager.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE && (!ProfileInfoCacheManager.isProfilePictureUploaded() || !ProfileInfoCacheManager.isIdentificationDocumentUploaded()
                                || !ProfileInfoCacheManager.isBasicInfoAdded() || !ProfileInfoCacheManager.isSourceOfFundAdded())) {
                            ((DeviceTrustActivity) getActivity()).switchToProfileCompletionHelperActivity();
                        } else {
                            if (((DeviceTrustActivity) getActivity()).transactionHistory != null) {
                                navigateToDesiredActivityViaWallet();
                            } else {
                                ((DeviceTrustActivity) getActivity()).switchToHomeActivity();
                            }
                        }
                    } else {
                        Toaster.makeText(getActivity(), mGetCardResponse.getMessage(), Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                }
                mGetAllAddedCards = null;
                break;
            default:
                mProgressDialog.dismiss();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void processTrustedDeviceList(String json) {
        Gson gson = new Gson();
        mGetTrustedDeviceResponse = gson.fromJson(json, GetTrustedDeviceResponse.class);
        mTrustedDeviceList = (ArrayList<TrustedDevice>) mGetTrustedDeviceResponse.getDevices();

        mTrustedDeviceAdapter = new TrustedDeviceAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mTrustedDevicesRecyclerView.setLayoutManager(mLayoutManager);
        mTrustedDevicesRecyclerView.setAdapter(mTrustedDeviceAdapter);

        setContentShown(true);
    }

    private void getBulkSignUpUserDetails() {
        if (mGetBulkSignupUserDetailsTask != null) {
            return;
        }
        mGetBulkSignupUserDetailsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS,
                Constants.BASE_URL_MM + Constants.URL_GET_BULK_SIGN_UP_USER_DETAILS, getActivity(), this, true);
        mGetBulkSignupUserDetailsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class TrustedDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public TrustedDeviceAdapter() {
        }

        public class TrustedDeviceViewHolder extends RecyclerView.ViewHolder {
            private final ImageView mDeviceImageView;
            private final TextView mDeviceNameView;
            private final TextView mGrantTimeView;
            private final ImageButton mRemoveTrustedDeviceButton;


            public TrustedDeviceViewHolder(final View itemView) {
                super(itemView);

                mDeviceImageView = (ImageView) itemView.findViewById(R.id.trusted_device_imageView);
                mDeviceNameView = (TextView) itemView.findViewById(R.id.textview_device_name);
                mGrantTimeView = (TextView) itemView.findViewById(R.id.textview_time);
                mRemoveTrustedDeviceButton = (ImageButton) itemView.findViewById(R.id.remove_trusted_device_button);
            }

            public void bindView(int pos) {

                final TrustedDevice trustedDevice = mTrustedDeviceList.get(pos);

                //Setting the correct image based on trusted device type
                int[] images = {
                        R.drawable.ic_browser3x,
                        R.drawable.ic_android3x,
                };

                final String deviceID = trustedDevice != null ? trustedDevice.getDeviceId() : null;
                String android = getString(R.string.android);
                String ios = getString(R.string.ios);
                String browser = getString(R.string.browser);

                if (deviceID.toLowerCase().contains(android.toLowerCase()))
                    mDeviceImageView.setImageResource(images[1]);
                else if (deviceID.toLowerCase().contains(ios.toLowerCase()))
                    mDeviceImageView.setImageResource(images[1]);
                else if (deviceID.toLowerCase().contains(browser.toLowerCase()))
                    mDeviceImageView.setImageResource(images[0]);

                mDeviceNameView.setText(trustedDevice.getDeviceName());
                mGrantTimeView.setText(trustedDevice.getCreatedTimeString());

                mRemoveTrustedDeviceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeviceRemoveConfirmationDialog(trustedDevice.getId());
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trusted_device_remove,
                    parent, false);

            return new TrustedDeviceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                TrustedDeviceViewHolder vh = (TrustedDeviceViewHolder) holder;
                vh.bindView(position);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mTrustedDeviceList != null)
                return mTrustedDeviceList.size();
            else return 0;
        }
    }
}