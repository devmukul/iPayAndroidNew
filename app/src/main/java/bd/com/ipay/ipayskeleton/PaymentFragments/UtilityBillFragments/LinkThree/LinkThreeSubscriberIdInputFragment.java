package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetLinkThreeSubscriberInfoResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractUserIdInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LinkThreeSubscriberIdInputFragment extends IPayAbstractUserIdInputFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetCustomerInfoTask;
    private AnimatedProgressDialog customProgressDialog;
    private final Gson gson = new Gson();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customProgressDialog = new AnimatedProgressDialog(getContext());
    }

    @Override
    protected void setupViewProperties() {
        setTitle(getString(R.string.link_three));
        setMerchantIconImage(R.drawable.link_three_logo);
        setInputMessage(getString(R.string.link_three_subscriber_id_input_message));
        setUserIdHint(getString(R.string.subscriber_id));
    }

    @Override
    protected boolean verifyInput() {
        if (TextUtils.isEmpty(getUserId())) {
            showErrorMessage(getString(R.string.enter_subscriber_id));
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void performContinueAction() {
        if (!Utilities.isConnectionAvailable(getContext())) {
            Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
        } else if (mGetCustomerInfoTask != null) {
            return;
        }
        customProgressDialog.showDialog();

        mGetCustomerInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_LINK_THREE_CUSTOMER_INFO,
                Constants.BASE_URL_UTILITY + Constants.URL_GET_LINK_THREE_CUSTOMER_INFO + getUserId(), getActivity(), this, false);
        mGetCustomerInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), customProgressDialog)) {
            mGetCustomerInfoTask = null;
            if (result != null && result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                            fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    Utilities.showErrorDialog(getContext(), genericResponseWithMessageOnly.getMessage());
                } catch (Exception e) {
                    Utilities.showErrorDialog(getContext(), getString(R.string.not_found));
                }
            }
            return;
        } else {
            switch (result.getApiCommand()) {
                case Constants.COMMAND_GET_LINK_THREE_CUSTOMER_INFO:
                    try {
                        final GetLinkThreeSubscriberInfoResponse linkThreeSubscriberInfoResponse = gson.fromJson(result.getJsonString(), GetLinkThreeSubscriberInfoResponse.class);
                        mGetCustomerInfoTask = null;
                        customProgressDialog.dismissDialog();
                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                Bundle bundle = new Bundle();
                                bundle.putString(LinkThreeBillAmountInputFragment.SUBSCRIBER_ID_KEY, getUserId());
                                bundle.putString(LinkThreeBillAmountInputFragment.USER_NAME_KEY, linkThreeSubscriberInfoResponse.getSubscriberName());
                                if (getActivity() != null)
                                    Utilities.hideKeyboard(getActivity());
                                if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
                                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new LinkThreeBillAmountInputFragment(), bundle, 1, true);
                                }
                                break;
                            default:
                                if (!TextUtils.isEmpty(linkThreeSubscriberInfoResponse.getMessage())) {
                                    Utilities.showErrorDialog(getContext(), linkThreeSubscriberInfoResponse.getMessage());
                                } else {
                                    Utilities.showErrorDialog(getContext(), getString(R.string.service_not_available));
                                }
                                break;
                        }
                    } catch (Exception e) {
                        customProgressDialog.dismissDialog();
                        Utilities.showErrorDialog(getContext(), getString(R.string.service_not_available));
                    }
                    break;
            }
        }
    }
}