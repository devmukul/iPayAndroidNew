package bd.com.ipay.ipayskeleton.Service.GCM;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.GetIdentificationDocumentResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.GetEmailResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.TrustedNetwork.GetTrustedPersonsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.GetTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToastandLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetProfileInfoResponse mGetProfileInfoResponse;

    private HttpRequestGetAsyncTask mGetIdentificationDocumentsTask = null;
    private GetIdentificationDocumentResponse mIdentificationDocumentResponse = null;

    private HttpRequestGetAsyncTask mGetEmailsTask = null;
    private GetEmailResponse mGetEmailResponse;

    private HttpRequestGetAsyncTask mGetBankTask = null;
    private GetEmailResponse mGetBankResponse;

    private HttpRequestGetAsyncTask mGetTrustedDeviceTask = null;
    private GetTrustedDeviceResponse mGetTrustedDeviceResponse = null;

    private HttpRequestGetAsyncTask mGetTrustedPersonsTask = null;
    private GetTrustedPersonsResponse mGetTrustedPersonsResponse = null;

    private String tag;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (Constants.DEBUG)
            Log.w("Push Found", "From: " + from + ", data: " + data);

        boolean isLoggedIn = ProfileInfoCacheManager.getLoggedInStatus(false);

        tag = data.getString(Constants.PUSH_NOTIFICATION_EVENT);

        switch (tag) {
            case Constants.PUSH_NOTIFICATION_TAG_PROFILE_PICTURE:
                if (isForeground() && isLoggedIn)
                    getProfileInfo();
                else {
                    PushNotificationStatusHolder.setUpdateNeeded(tag, true);
                    createNotification(getString(R.string.push_profile_picture_updated_title),
                            getString(R.string.push_profile_picture_updated_message));
                }
                break;
            case Constants.PUSH_NOTIFICATION_TAG_PROFILE_INFO_UPDATE:
                if (isForeground() && isLoggedIn)
                    getProfileInfo();
                else {
                    createNotification(getString(R.string.push_profile_info_updated_title),
                            getString(R.string.push_profile_info_updated_message));
                    PushNotificationStatusHolder.setUpdateNeeded(tag, true);
                }
                break;
            case Constants.PUSH_NOTIFICATION_TAG_IDENTIFICATION_DOCUMENT_UPDATE:
                if (isForeground() && isLoggedIn)
                    getIdentificationDocuments();
                else {
                    createNotification(getString(R.string.push_identification_document_updated_title),
                            getString(R.string.push_identification_document_updated_message));
                    PushNotificationStatusHolder.setUpdateNeeded(tag, true);
                }
                break;
            case Constants.PUSH_NOTIFICATION_TAG_EMAIL_UPDATE:
                if (isForeground() && isLoggedIn)
                    getEmails();
                else {
                    createNotification(getString(R.string.push_email_updated_title),
                            getString(R.string.push_email_updated_message));
                    PushNotificationStatusHolder.setUpdateNeeded(tag, true);
                }
                break;
            case Constants.PUSH_NOTIFICATION_TAG_BANK_UPDATE:
                if (isForeground() && isLoggedIn)
                    getBankList();
                else {
                    createNotification(getString(R.string.push_bank_updated_title),
                            getString(R.string.push_bank_updated_message));
                    PushNotificationStatusHolder.setUpdateNeeded(tag, true);
                }
                break;
            case Constants.PUSH_NOTIFICATION_TAG_DEVICE_UPDATE:
                if (isForeground() && isLoggedIn)
                    getTrustedDeviceList();
                else {
                    createNotification(getString(R.string.push_device_updated_title),
                            getString(R.string.push_device_updated_message));
                    PushNotificationStatusHolder.setUpdateNeeded(tag, true);
                }
                break;
            case Constants.PUSH_NOTIFICATION_TAG_TRUSTED_PERSON_UPDATE:
                if (isForeground() && isLoggedIn)
                    getTrustedPersons();
                else {
                    createNotification(getString(R.string.push_trusted_person_updated_title),
                            getString(R.string.push_trusted_person_updated_message));
                    PushNotificationStatusHolder.setUpdateNeeded(tag, true);
                }
                break;
            case Constants.PUSH_NOTIFICATION_TAG_TRANSACTION:
                if (isForeground() && isLoggedIn) {
                    Logger.logDebug("Transaction", "Sending broadcast");
                    Utilities.sendBroadcast(this, Constants.TRANSACTION_HISTORY_UPDATE_BROADCAST);
                }
        }
    }

    private void createNotification(String title, String message) {
        int notificationID = new Random().nextInt();
        Intent intent = new Intent(this, SignupOrLoginActivity.class);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    private boolean isForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(Constants.ApplicationPackage);
    }

    private void getProfileInfo() {
        if (mGetProfileInfoTask != null) {
            return;
        }

        mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_INFO_REQUEST, this, this);
        mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getIdentificationDocuments() {
        if (mGetIdentificationDocumentsTask != null) {
            return;
        }

        mGetIdentificationDocumentsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_IDENTIFICATION_DOCUMENTS_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_GET_DOCUMENTS, this, this);
        mGetIdentificationDocumentsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getEmails() {
        if (mGetEmailsTask != null) {
            return;
        }

        mGetEmailsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_EMAILS,
                Constants.BASE_URL_MM + Constants.URL_GET_EMAIL, this, this);
        mGetEmailsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getBankList() {
        if (mGetBankTask != null) {
            return;
        }

        mGetBankTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_BANK, this);
        mGetBankTask.mHttpResponseListener = this;
        mGetBankTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getTrustedDeviceList() {
        if (mGetTrustedDeviceTask != null) {
            return;
        }

        mGetTrustedDeviceTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRUSTED_DEVICES,
                Constants.BASE_URL_MM + Constants.URL_GET_TRUSTED_DEVICES, this, this);
        mGetTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getTrustedPersons() {
        if (mGetTrustedPersonsTask != null) {
            return;
        }

        mGetTrustedPersonsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRUSTED_PERSONS,
                Constants.BASE_URL_MM + Constants.URL_GET_TRUSTED_PERSONS, this, this);
        mGetTrustedPersonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
//            mUserInfoTask = null;
            return;
        }

        DataHelper dataHelper = DataHelper.getInstance(this);
        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_PROFILE_INFO_REQUEST:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    try {
                        mGetProfileInfoResponse = gson.fromJson(result.getJsonString(), GetProfileInfoResponse.class);
                        dataHelper.updatePushEvents(Constants.PUSH_NOTIFICATION_TAG_PROFILE_INFO_UPDATE, result.getJsonString());
                        PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_PROFILE_INFO_UPDATE, false);
                        PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_PROFILE_PICTURE, false);

                        String imageUrl = Utilities.getImage(mGetProfileInfoResponse.getProfilePictures(), Constants.IMAGE_QUALITY_HIGH);

                        ProfileInfoCacheManager.updateCache(mGetProfileInfoResponse.getName(),
                                mGetProfileInfoResponse.getMobileNumber(), imageUrl, mGetProfileInfoResponse.getVerificationStatus());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mGetProfileInfoTask = null;
                break;
            case Constants.COMMAND_GET_IDENTIFICATION_DOCUMENTS_REQUEST:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    dataHelper.updatePushEvents(Constants.PUSH_NOTIFICATION_TAG_IDENTIFICATION_DOCUMENT_UPDATE, result.getJsonString());
                    PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_IDENTIFICATION_DOCUMENT_UPDATE, false);
                }

                mGetIdentificationDocumentsTask = null;
                break;
            case Constants.COMMAND_GET_EMAILS:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    dataHelper.updatePushEvents(Constants.PUSH_NOTIFICATION_TAG_EMAIL_UPDATE, result.getJsonString());
                    PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_EMAIL_UPDATE, false);
                }

                mGetEmailsTask = null;
                break;
            case Constants.COMMAND_GET_BANK_LIST:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    dataHelper.updatePushEvents(Constants.PUSH_NOTIFICATION_TAG_BANK_UPDATE, result.getJsonString());
                    PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_BANK_UPDATE, false);
                }

                mGetBankTask = null;
                break;
            case Constants.COMMAND_GET_TRUSTED_DEVICES:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    dataHelper.updatePushEvents(Constants.PUSH_NOTIFICATION_TAG_DEVICE_UPDATE, result.getJsonString());
                    PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_DEVICE_UPDATE, false);
                }

                mGetTrustedDeviceTask = null;
                break;
            case Constants.COMMAND_GET_TRUSTED_PERSONS:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    dataHelper.updatePushEvents(Constants.PUSH_NOTIFICATION_TAG_TRUSTED_PERSON_UPDATE, result.getJsonString());
                    PushNotificationStatusHolder.setUpdateNeeded(Constants.PUSH_NOTIFICATION_TAG_TRUSTED_PERSON_UPDATE, false);
                }

                mGetTrustedPersonsTask = null;
                break;
        }

    }
}