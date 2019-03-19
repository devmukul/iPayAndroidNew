package bd.com.ipay.ipayskeleton.Api.NotificationApi;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RefreshToken.FCMRefreshTokenRequest;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;

public class RegisterFCMTokenToServerAsyncTask implements HttpResponseListener {

    private HttpRequestPostAsyncTask mRefreshTokenAsyncTask = null;
    private String refreshedToken;
    private Context context;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    public RegisterFCMTokenToServerAsyncTask(Context context) {
        this.context = context;
        Logger.logD("Firebase Token", "Refreshed token: " + refreshedToken);

        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        Logger.logW("Firebase Token", "Refresh token called");

        if (mRefreshTokenAsyncTask != null) {
            mRefreshTokenAsyncTask = null;
        }

        String myDeviceID = DeviceInfoFactory.getDeviceId(context);
        FCMRefreshTokenRequest mFcmRefreshTokenRequest = new FCMRefreshTokenRequest(refreshedToken, myDeviceID, Constants.MOBILE_ANDROID);
        Gson gson = new Gson();
        String json = gson.toJson(mFcmRefreshTokenRequest);
        mRefreshTokenAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REFRESH_FIREBASE_TOKEN,
                Constants.BASE_URL_PUSH_NOTIFICATION + Constants.URL_REFRESH_FIREBASE_TOKEN, json, context, true);
        mRefreshTokenAsyncTask.mHttpResponseListener = this;

        mRefreshTokenAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, context, null)) {
            mRefreshTokenAsyncTask = null;
            return;
        }
        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_REFRESH_FIREBASE_TOKEN)) {
            try {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    ProfileInfoCacheManager.setPushNotificationToken(null);
                } else {
                    if (this != null)
                        Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            mRefreshTokenAsyncTask = null;
        }
    }
}

