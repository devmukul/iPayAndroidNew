package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.Carnival;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.CarnivalCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractUserIdInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeBillAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BillDetailsDialog;

public class CarnivalIdInputFragment extends IPayAbstractUserIdInputFragment implements HttpResponseListener {

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

        if (getArguments() != null){
            isFromSaved = getArguments().getBoolean("IS_FROM_HISTORY", false);
            userId = getArguments().getString(Constants.ACCOUNT_ID);
            amount = getArguments().getString(Constants.AMOUNT);
            metaDataText =  getArguments().getString("META_DATA");
        }
	}

    @Override
    protected void setupViewProperties() {
        setTitle(getString(R.string.carnival));
        setMerchantIconImage(R.drawable.ic_carnival);
        setInputMessage(getString(R.string.carnival_id_input_message));
        setUserIdHint(getString(R.string.carnival_id));

        if(isFromSaved && !TextUtils.isEmpty(userId)){
            setUserId(userId);
            MetaData metaData = new Gson().fromJson(metaDataText, MetaData.class);

            if(metaData!=null){
                setOtherPersonChecked(true);
                setOtherPersonName(metaData.getNotification().getSubscriberName());
                setOtherPersonMobile(metaData.getNotification().getMobileNumber());
            }else{
                setOtherPersonChecked(false);
            }
        }
    }

    @Override
    protected boolean verifyInput() {
        if (TextUtils.isEmpty(getUserId())) {
            showErrorMessage(getString(R.string.enter_carnival_id));
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
            } else if (mobileNumber.equals(ContactEngine.formatLocalMobileNumber(getOtherPersonMobile()))) {
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
		if (!Utilities.isConnectionAvailable(getContext())) {
			Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
		} else if (mGetCustomerInfoTask != null) {
			return;
		}
		customProgressDialog.showDialog();

        mGetCustomerInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_CARNIVAL_CUSTOMER_INFO,
                Constants.BASE_URL_UTILITY + Constants.URL_CARNIVAL + getUserId(), getActivity(), this, false);
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
                case Constants.COMMAND_GET_CARNIVAL_CUSTOMER_INFO:
                    mGetCustomerInfoTask = null;
                    final CarnivalCustomerInfoResponse carnivalCustomerInfoResponse = gson.fromJson(result.getJsonString(), CarnivalCustomerInfoResponse.class);
                    customProgressDialog.dismissDialog();
                    mGetCustomerInfoTask = null;
                    switch (result.getStatus()) {
                        case Constants.HTTP_RESPONSE_STATUS_OK:
                            showCarnivalUserInfo(carnivalCustomerInfoResponse);
                            break;
                        default:
                            if (!TextUtils.isEmpty(carnivalCustomerInfoResponse.getMessage())) {
                                Utilities.showErrorDialog(getContext(), carnivalCustomerInfoResponse.getMessage());
                            } else {
                                Utilities.showErrorDialog(getContext(), getString(R.string.service_not_available));
                            }
                            break;
                    }
                    break;
            }
        }
    }

    private void showCarnivalUserInfo(final CarnivalCustomerInfoResponse carnivalCustomerInfoResponse) {
        if (getActivity() == null)
            return;

        final BillDetailsDialog billDetailsDialog = new BillDetailsDialog(getContext());
        billDetailsDialog.setTitle(getString(R.string.bill_details));
        billDetailsDialog.setClientLogoImageResource(R.drawable.ic_carnival);
        billDetailsDialog.setBillTitleInfo(getUserId());
        billDetailsDialog.setBillSubTitleInfo(carnivalCustomerInfoResponse.getName());
        billDetailsDialog.setTotalBillInfo(getString(R.string.package_rate).toUpperCase(), new BigDecimal(carnivalCustomerInfoResponse.getCurrentPackageRate()).intValue());

        billDetailsDialog.setCloseButtonAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billDetailsDialog.cancel();
            }
        });
        billDetailsDialog.setPayBillButtonAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billDetailsDialog.cancel();
                Bundle bundle = new Bundle();
                bundle.putString(CarnivalBillAmountInputFragment.CARNIVAL_ID_KEY, getUserId());
                bundle.putString(CarnivalBillAmountInputFragment.USER_NAME_KEY, carnivalCustomerInfoResponse.getName());
                bundle.putInt(CarnivalBillAmountInputFragment.PACKAGE_RATE_KEY, new BigDecimal(carnivalCustomerInfoResponse.getCurrentPackageRate()).intValue());
                bundle.putString(CarnivalBillAmountInputFragment.OTHER_PERSON_NAME_KEY, getOtherPersonName());
                bundle.putString(CarnivalBillAmountInputFragment.OTHER_PERSON_MOBILE_KEY, ContactEngine.formatMobileNumberBD(getOtherPersonMobile()) );

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
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new CarnivalBillAmountInputFragment(), bundle, maxBackStack, true);
                }

            }
        });
        billDetailsDialog.show();
    }
}
