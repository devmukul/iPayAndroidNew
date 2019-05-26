package bd.com.ipay.ipayskeleton.Api.GenericApi;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseParser;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.OkHttpResponse;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Configuration.ApiVersionResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.SSLPinning;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class HttpRequestAsyncTask extends AsyncTask<Void, Void, GenericHttpResponse> {

    public HttpResponseListener mHttpResponseListener;

    final public String mUri;
    private final Context mContext;
    private final String API_COMMAND;
    private OkHttpResponse mHttpResponse;
    private String socketTimeOutConnection;
    private boolean isSlient;

    private boolean error = false;

    HttpRequestAsyncTask(String API_COMMAND, String mUri, Context mContext, HttpResponseListener listener, boolean isSilent) {
        this.API_COMMAND = API_COMMAND;
        this.mUri = mUri;
        this.mContext = mContext;
        this.mHttpResponseListener = listener;
        socketTimeOutConnection = null;
        this.isSlient = isSilent;
    }

    @Override
    protected GenericHttpResponse doInBackground(Void... params) {

        GenericHttpResponse mGenericHttpResponse = null;

        try {
            if (Utilities.isConnectionAvailable(mContext)) {
                String responseFromSSL = SSLPinning.validatePinning();
                if (responseFromSSL.equals(mContext.getString(R.string.OK))) {
                    if (Constants.IS_API_VERSION_CHECKED && !Constants.HAS_COME_FROM_BACKGROUND_TO_FOREGROUND) {
                        mHttpResponse = makeRequest();
                        mGenericHttpResponse = parseHttpResponse(mHttpResponse.getResponse());
                        mGenericHttpResponse.setUpdateNeeded(false);
                    } else {
                        mHttpResponse = makeApiVersionCheckRequest();
                        mGenericHttpResponse = parseHttpResponse(mHttpResponse.getResponse());
                        Constants.HAS_COME_FROM_BACKGROUND_TO_FOREGROUND = false;

                        // Validate the Api version and set whether the update is required or not
                        mGenericHttpResponse = validateApiVersion(mGenericHttpResponse);
                    }
                } else {
                    return new GenericHttpResponse(responseFromSSL, true);
                }
            } else {
                return new GenericHttpResponse(mContext.getString(R.string.no_internet_connection), isSlient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mGenericHttpResponse;

    }

    @Override
    protected void onPostExecute(final GenericHttpResponse result) {
        if (error) {
            if (mContext != null)
                Toast.makeText(mContext, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (result == null)
                Logger.logE(Constants.RESULT, API_COMMAND + " NULL");
            else
                Logger.logW(Constants.RESULT, Constants.GET_REQUEST + result.toString());

        if (result != null) {
            result.setSilent(isSlient);
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_UNAUTHORIZED) {

                    try {
                        MyApplication myApplicationInstance = MyApplication.getMyApplicationInstance();
                        boolean loggedIn = ProfileInfoCacheManager.getLoggedInStatus(true);

                        if (loggedIn && !result.getJsonString().contains(Constants.USERNAME_PASSWORD_INCORRECT)) {
                            String message = mContext.getString(R.string.please_log_in_again);
                            myApplicationInstance.launchLoginPage(message);
                        } else {
                            // Wrong user name or password returns HTTP_RESPONSE_STATUS_UNAUTHORIZED too
                            if (mHttpResponseListener != null)
                                mHttpResponseListener.httpResponseReceiver(result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    if (mHttpResponseListener != null)
                        mHttpResponseListener.httpResponseReceiver(result);
                }

            } else {
                if (socketTimeOutConnection == null) {
                    if (mHttpResponseListener != null)
                        mHttpResponseListener.httpResponseReceiver(null);
                } else {
                    if (mHttpResponseListener != null) {
                        mHttpResponseListener.httpResponseReceiver(new GenericHttpResponse(socketTimeOutConnection, isSlient));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancelled() {
        mHttpResponseListener.httpResponseReceiver(null);
    }

    private OkHttpResponse makeRequest() {

        final OkHttpResponse okHttpResponse = new OkHttpResponse();
        try {
            Request request = getRequest();
            if (MyApplication.getMyApplicationInstance().getOkHttpClient() != null) {
                try {
                    Response response = MyApplication.getMyApplicationInstance().getOkHttpClient().newCall(request).execute();
                    okHttpResponse.setResponse(response);
                } catch (IOException e) {

                    e.printStackTrace();

                    if (e instanceof SocketException) {
                        socketTimeOutConnection = mContext.getString(R.string.network_unreachable);
                    } else if (e instanceof SocketTimeoutException) {
                        socketTimeOutConnection = mContext.getString(R.string.connection_time_out);
                    }

                }
            } else {
                OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    okHttpResponse.setResponse(response);
                } catch (IOException e) {
                    if (e instanceof SocketException) {
                        socketTimeOutConnection = mContext.getString(R.string.network_unreachable);
                    } else if (e instanceof SocketTimeoutException) {
                        socketTimeOutConnection = mContext.getString(R.string.connection_time_out);
                    }

                }
            }
            return okHttpResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return okHttpResponse;

    }

    private OkHttpResponse makeApiVersionCheckRequest() {
        final OkHttpResponse okHttpResponse = new OkHttpResponse();
        try {
            Request request = new Request.Builder().
                    header(Constants.USER_AGENT, Constants.USER_AGENT_MOBILE_ANDROID)
                    .header("Accept", "application/json")
                    .header("Content-type", "application/json")
                    .get()
                    .url(Constants.BASE_URL_MM + Constants.URL_GET_MIN_API_VERSION_REQUIRED)
                    .build();
            OkHttpClient okHttpClient;
            Response response;
            if (MyApplication.getMyApplicationInstance().getOkHttpClient() != null) {
                okHttpClient = MyApplication.getMyApplicationInstance().getOkHttpClient();
            } else {
                okHttpClient = new OkHttpClient.Builder().
                        readTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .build();
            }
            try {
                response = okHttpClient.newCall(request).execute();
                okHttpResponse.setResponse(response);
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    socketTimeOutConnection = mContext.getString(R.string.network_unreachable);
                } else if (e instanceof SocketTimeoutException) {
                    socketTimeOutConnection = mContext.getString(R.string.connection_time_out);
                }
            }

            return okHttpResponse;

        } catch (Exception e) {

        }
        return okHttpResponse;
    }

    private GenericHttpResponse parseHttpResponse(Response response) {
        GenericHttpResponse mGenericHttpResponse = null;

        if (response == null)
            return mGenericHttpResponse;

        HttpResponseParser mHttpResponseParser = new HttpResponseParser();
        mHttpResponseParser.setAPI_COMMAND(API_COMMAND);
        mHttpResponseParser.setHttpResponse(response);
        mGenericHttpResponse = mHttpResponseParser.parseHttpResponse();

        // Set the context, after response is parsed.
        mGenericHttpResponse.setContext(mContext);

        return mGenericHttpResponse;
    }

    private GenericHttpResponse validateApiVersion(GenericHttpResponse mGenericHttpResponse) {

        Gson gson = new Gson();

        try {
            ApiVersionResponse mApiVersionResponse = gson.fromJson(mGenericHttpResponse.getJsonString(), ApiVersionResponse.class);

            if (mGenericHttpResponse.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                if (mApiVersionResponse != null) {
                    int requiredAPIVersion = mApiVersionResponse.getAndroid();
                    int availableAPIVersion = BuildConfig.VERSION_CODE;

                    if (availableAPIVersion < requiredAPIVersion) {
                        mGenericHttpResponse.setUpdateNeeded(true);
                    } else {
                        Constants.IS_API_VERSION_CHECKED = true;
                        Constants.HAS_COME_FROM_BACKGROUND_TO_FOREGROUND = false;
                        mHttpResponse = makeRequest();
                        mGenericHttpResponse = parseHttpResponse(mHttpResponse.getResponse());
                        mGenericHttpResponse.setUpdateNeeded(false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mGenericHttpResponse;
    }

    Context getContext() {
        return mContext;
    }

    abstract protected Request getRequest();
}