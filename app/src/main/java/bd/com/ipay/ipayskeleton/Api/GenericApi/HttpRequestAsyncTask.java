package bd.com.ipay.ipayskeleton.Api.GenericApi;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseParser;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Configuration.ApiVersionResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.SSLPinning;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.TokenManager;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public abstract class HttpRequestAsyncTask extends AsyncTask<Void, Void, GenericHttpResponse> {

    public HttpResponseListener mHttpResponseListener;

    final String mUri;
    private final Context mContext;
    private final String API_COMMAND;
    private HttpResponse mHttpResponse;

    private boolean error = false;

    HttpRequestAsyncTask(String API_COMMAND, String mUri, Context mContext, HttpResponseListener listener) {
        this.API_COMMAND = API_COMMAND;
        this.mUri = mUri;
        this.mContext = mContext;
        this.mHttpResponseListener = listener;
    }

    @Override
    protected GenericHttpResponse doInBackground(Void... params) {

        GenericHttpResponse mGenericHttpResponse = null;

        try {
            if (SSLPinning.validatePinning()) {
                if (Utilities.isConnectionAvailable(mContext)) {
                    if (Constants.IS_API_VERSION_CHECKED) {
                        mHttpResponse = makeRequest();
                        mGenericHttpResponse = parseHttpResponse(mHttpResponse);
                        mGenericHttpResponse.setUpdateNeeded(false);
                    } else {
                        mHttpResponse = makeApiVersionCheckRequest();
                        mGenericHttpResponse = parseHttpResponse(mHttpResponse);

                        // Validate the Api version and set whether the update is required or not
                        mGenericHttpResponse = validateApiVersion(mGenericHttpResponse);
                    }

                } else {
                    Logger.logD(Constants.ERROR, API_COMMAND);
                    error = true;
                    return null;
                }
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

        if (result == null)
            Logger.logE(Constants.RESULT, API_COMMAND + " NULL");
        else
            Logger.logW(Constants.RESULT, Constants.GET_REQUEST + result.toString());

        if (result != null) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_UNAUTHORIZED) {

                try {
                    MyApplication myApplicationInstance = MyApplication.getMyApplicationInstance();
                    boolean loggedIn = ProfileInfoCacheManager.getLoggedInStatus(true);

                    if (loggedIn && !result.getJsonString().contains(Constants.USERNAME_PASSWORD_INCORRECT)) {
                        String message = mContext.getString(R.string.please_log_in_again);
                        myApplicationInstance.launchLoginPage(message);
                        Utilities.resetIntercomInformation();
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
            if (mHttpResponseListener != null)
                mHttpResponseListener.httpResponseReceiver(null);
        }
    }

    @Override
    protected void onCancelled() {
        mHttpResponseListener.httpResponseReceiver(null);
    }

    private HttpResponse makeRequest() {
        try {
            HttpRequestBase httpRequest = getRequest();

            if (TokenManager.getToken().length() > 0)
                httpRequest.setHeader(Constants.TOKEN, TokenManager.getToken());
            if (TokenManager.isEmployerAccountActive())
                httpRequest.setHeader(Constants.OPERATING_ON_ACCOUNT_ID, TokenManager.getOperatingOnAccountId());
            httpRequest.setHeader(Constants.USER_AGENT, Constants.USER_AGENT_MOBILE_ANDROID);
            httpRequest.setHeader("Accept", "application/json");
            httpRequest.setHeader("Content-type", "application/json");

            HttpParams httpParams = new BasicHttpParams();
            HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
            HttpProtocolParams.setHttpElementCharset(httpParams, HTTP.UTF_8);
            HttpClient client = new DefaultHttpClient(httpParams);

            return client.execute(httpRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpResponse makeApiVersionCheckRequest() {
        try {
            HttpRequestBase httpRequest = new HttpGet(Constants.BASE_URL_MM + Constants.URL_GET_MIN_API_VERSION_REQUIRED);

            httpRequest.setHeader(Constants.USER_AGENT, Constants.USER_AGENT_MOBILE_ANDROID);
            httpRequest.setHeader("Accept", "application/json");
            httpRequest.setHeader("Content-type", "application/json");

            HttpParams httpParams = new BasicHttpParams();
            HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
            HttpProtocolParams.setHttpElementCharset(httpParams, HTTP.UTF_8);
            HttpClient client = new DefaultHttpClient(httpParams);

            return client.execute(httpRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GenericHttpResponse parseHttpResponse(HttpResponse mHttpResponse) {
        GenericHttpResponse mGenericHttpResponse = null;

        if (mHttpResponse == null)
            return mGenericHttpResponse;

        HttpResponseParser mHttpResponseParser = new HttpResponseParser();
        mHttpResponseParser.setAPI_COMMAND(API_COMMAND);
        mHttpResponseParser.setHttpResponse(mHttpResponse);
        mHttpResponseParser.setContext(mContext);

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
                        mHttpResponse = makeRequest();
                        mGenericHttpResponse = parseHttpResponse(mHttpResponse);
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

    abstract protected HttpRequestBase getRequest();
}