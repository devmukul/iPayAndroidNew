package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.COLBD;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class COLBDSubscriberIdInputFragment extends IPayAbstractUserIdInputFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetCustomerInfoTask;
    private AnimatedProgressDialog customProgressDialog;
    private final Gson gson = new Gson();
    private String userId;
    private String amount;
    private boolean isFromSaved;
    private String metaDataText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customProgressDialog = new AnimatedProgressDialog(getContext());
    }

    @Override
    protected void setupViewProperties() {
        setTitle("COLBD");
        setMerchantIconImage(R.drawable.colbd );
        setInputMessage(getString(R.string.subscriber_id_input_message));
        setUserIdHint(getString(R.string.subscriber_id));
        hidePayingForOtherPerson();
    }

    @Override
    protected boolean verifyInput() {
        if (TextUtils.isEmpty(getUserId())) {
            showErrorMessage(getString(R.string.enter_subscriber_id));
            return false;
        } else if (ifPayingForOtherPerson()) {
            String mobileNumber = ProfileInfoCacheManager.getMobileNumber();
            if (TextUtils.isEmpty(getOtherPersonName())) {
                showErrorMessage(getString(R.string.enter_name));
                return false;
            } else if (TextUtils.isEmpty(getOtherPersonMobile())) {
                showErrorMessage(getString(R.string.enter_mobile_number));
                return false;
            } else if (!InputValidator.isValidMobileNumberBD(getOtherPersonMobile())) {
                showErrorMessage(getString(R.string.please_enter_valid_mobile_number));
                return false;
            } else if (mobileNumber.equals(ContactEngine.formatMobileNumberBD(getOtherPersonMobile()))) {
                showErrorMessage(getString(R.string.you_can_not_give_own_number));
                return false;
            }
            else{
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void performContinueAction() {
        Bundle bundle = new Bundle();
        bundle.putString(COLBDBillAmountInputFragment.SUBSCRIBER_ID_KEY, getUserId());


        if (getActivity() != null)
            Utilities.hideKeyboard(getActivity());
        if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
            int maxBackStack=1;
            ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new COLBDBillAmountInputFragment(), bundle, maxBackStack, true);
        }

//        if (!Utilities.isConnectionAvailable(getContext())) {
//            Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
//        } else if (mGetCustomerInfoTask != null) {
//            return;
//        }
//        customProgressDialog.showDialog();
//
//        mGetCustomerInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_LINK_THREE_CUSTOMER_INFO,
//                Constants.BASE_URL_UTILITY + Constants.URL_GET_LINK_THREE_CUSTOMER_INFO + getUserId(), getActivity(), this, false);
//        mGetCustomerInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                                bundle.putString(COLBDBillAmountInputFragment.SUBSCRIBER_ID_KEY, getUserId());
                                bundle.putString(COLBDBillAmountInputFragment.USER_NAME_KEY, linkThreeSubscriberInfoResponse.getSubscriberName());
                                bundle.putString(COLBDBillAmountInputFragment.OTHER_PERSON_NAME_KEY, getOtherPersonName());
                                bundle.putString(COLBDBillAmountInputFragment.OTHER_PERSON_MOBILE_KEY, ContactEngine.formatMobileNumberBD(getOtherPersonMobile()) );

                                if(isFromSaved) {
                                    bundle.putBoolean("IS_FROM_HISTORY", true);
                                    bundle.putString(Constants.AMOUNT, amount);
                                }

                                if (getActivity() != null)
                                    Utilities.hideKeyboard(getActivity());
                                if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
                                    int maxBackStack=1;
                                    if(isFromSaved)
                                        maxBackStack =2;
                                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new COLBDBillAmountInputFragment(), bundle, maxBackStack, true);
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