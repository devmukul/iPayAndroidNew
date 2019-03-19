package bd.com.ipay.ipayskeleton.Api.GenericApi;

import android.content.Context;

import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.TokenManager;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpRequestPostAsyncTask extends HttpRequestAsyncTask {

    private final String mJsonString;

    private String pinAsHeader;
    private String otpAsHeader;

    private long sponsorAccountId;

    public HttpRequestPostAsyncTask(String API_COMMAND, String mUri, String mJsonString,
                                    Context mContext, HttpResponseListener listener, boolean isSilent) {
        super(API_COMMAND, mUri, mContext, listener, isSilent);
        this.mJsonString = mJsonString;
    }

    public HttpRequestPostAsyncTask(String API_COMMAND, String mUri, String mJsonString, Context mContext, boolean isSilent) {
        this(API_COMMAND, mUri, mJsonString, mContext, null, isSilent);
    }

    public String getPinAsHeader() {
        return pinAsHeader;
    }

    public void setPinAsHeader(String pinAsHeader) {
        this.pinAsHeader = pinAsHeader;
    }

    public String getOtpAsHeader() {
        return otpAsHeader;
    }

    public void setOtpAsHeader(String otpAsHeader) {
        this.otpAsHeader = otpAsHeader;
    }

    public long getSponsorAccountId() {
        return sponsorAccountId;
    }

    public void setSponsorAccountId(long sponsorAccountId) {
        this.sponsorAccountId = sponsorAccountId;
    }

    @Override
    protected Request getRequest() {
        Logger.logW("POST_URL", mUri);
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody;
        try {
            requestBody = RequestBody.create(JSON, mJsonString);
        } catch (Exception e) {
            requestBody = RequestBody.create(JSON, "");
        }
        if (mJsonString != null)
            Logger.logW("json", mJsonString);
        Request.Builder requestBuilder = new Request.Builder().
                header(Constants.USER_AGENT, Constants.USER_AGENT_MOBILE_ANDROID)
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .header(Constants.TOKEN, TokenManager.getToken())
                .post(requestBody)
                .url(mUri);
        if (this.pinAsHeader != null && !this.pinAsHeader.isEmpty()) {
            requestBuilder.header(Constants.X_IPAY_PIN, pinAsHeader);
        }
        if (this.otpAsHeader != null && !this.otpAsHeader.isEmpty()) {
            requestBuilder.header(Constants.X_IPAY_OTP, otpAsHeader);
        }
        if (this.sponsorAccountId >0) {
            requestBuilder.header(Constants.SPONSOR_ACCOUNT_ID_AS_HEADER, Long.toString(sponsorAccountId));
        }
        if (TokenManager.getOnAccountId() != null && TokenManager.getOnAccountId() != "") {
            requestBuilder.header(Constants.OPERATING_ON_ACCOUNT_ID, TokenManager.getOnAccountId());
        }

        return requestBuilder.build();
    }
}