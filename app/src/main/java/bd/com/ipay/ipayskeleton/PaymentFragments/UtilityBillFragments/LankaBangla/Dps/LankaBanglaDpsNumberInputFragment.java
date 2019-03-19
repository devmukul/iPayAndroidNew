package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaDpsUserInfoResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractUserIdInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.DpsBillDetailsDialog;

public class LankaBanglaDpsNumberInputFragment extends IPayAbstractUserIdInputFragment implements HttpResponseListener {

	private HttpRequestGetAsyncTask mGetLankaBanglaDpsUserInfoAsyncTask = null;
	private final Gson gson = new GsonBuilder().create();
	private AnimatedProgressDialog customProgressDialog;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customProgressDialog = new AnimatedProgressDialog(getContext());
	}

    @Override
    protected void performContinueAction() {
        if (!Utilities.isConnectionAvailable(getContext())) {
            Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
        } else if (mGetLankaBanglaDpsUserInfoAsyncTask != null) {
            return;
        }

		String url = Constants.BASE_URL_UTILITY + Constants.LANKABANGLA_DPS_USER + getUserId();
		mGetLankaBanglaDpsUserInfoAsyncTask = new HttpRequestGetAsyncTask(
				Constants.COMMAND_GET_LANKABANGLA_DPS_CUSTOMER_INFO, url, getContext(), false);
		mGetLankaBanglaDpsUserInfoAsyncTask.mHttpResponseListener = this;
		customProgressDialog.showDialog();
		mGetLankaBanglaDpsUserInfoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

    @Override
    public boolean verifyInput() {
        if (TextUtils.isEmpty(getUserId())) {
            showErrorMessage(getString(R.string.empty_dps_number_message));
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void setupViewProperties() {
        setTitle(getString(R.string.lanka_bangla_dps));
        setMerchantIconImage(R.drawable.ic_lankabd2);
        setInputMessage(getString(R.string.lanka_bangla_dps_number_input_message));
        setUserIdHint(getString(R.string.lanka_bangla_dps_number));
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
            customProgressDialog.dismissDialog();
            mGetLankaBanglaDpsUserInfoAsyncTask = null;
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
            try {
                customProgressDialog.dismissDialog();
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_GET_LANKABANGLA_DPS_CUSTOMER_INFO:
                        mGetLankaBanglaDpsUserInfoAsyncTask = null;
                        LankaBanglaDpsUserInfoResponse lankaBanglaDpsUserInfoResponse = gson.fromJson(result.getJsonString(), LankaBanglaDpsUserInfoResponse.class);
                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                showLankaBanglaUserInfo(lankaBanglaDpsUserInfoResponse);
                                break;
                            default:
                                if (!TextUtils.isEmpty(lankaBanglaDpsUserInfoResponse.getMessage())) {
                                    Utilities.showErrorDialog(getContext(), lankaBanglaDpsUserInfoResponse.getMessage());
                                } else {
                                    Utilities.showErrorDialog(getContext(), getString(R.string.service_not_available));
                                }
                                break;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Utilities.showErrorDialog(getContext(), getString(R.string.service_not_available));
            }
        }
        customProgressDialog.dismissDialog();
    }

    private void showLankaBanglaUserInfo(final LankaBanglaDpsUserInfoResponse lankaBanglaCustomerInfoResponse) {
        if (getActivity() == null)
            return;

        final DpsBillDetailsDialog billDetailsDialog = new DpsBillDetailsDialog(getContext());
        billDetailsDialog.setTitle(getString(R.string.bill_details));
        billDetailsDialog.setClientLogoImageResource(R.drawable.ic_lankabd2);

        billDetailsDialog.setBillTitleInfo(lankaBanglaCustomerInfoResponse.getAccountNumber());
        billDetailsDialog.setBillSubTitleInfo(lankaBanglaCustomerInfoResponse.getAccountTitle());
        billDetailsDialog.setAccountName(lankaBanglaCustomerInfoResponse.getAccountTitle());
        billDetailsDialog.setAccountNumber(lankaBanglaCustomerInfoResponse.getAccountNumber());
        billDetailsDialog.setBranchID(lankaBanglaCustomerInfoResponse.getBranchId());
        billDetailsDialog.setMaturityDate(lankaBanglaCustomerInfoResponse.getAccountMaturityDate());
        billDetailsDialog.setInstallmentAmount(Long.toString(lankaBanglaCustomerInfoResponse.getInstallmentAmount()));

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
                bundle.putString(LankaBanglaDpsAmountInputFragment.ACCOUNT_NUMBER_KEY, getUserId());
                bundle.putString(LankaBanglaDpsAmountInputFragment.ACCOUNT_USER_NAME_KEY, lankaBanglaCustomerInfoResponse.getAccountTitle());
                bundle.putString(LankaBanglaDpsAmountInputFragment.INSTALLMENT_AMOUNT_KEY, Long.toString(lankaBanglaCustomerInfoResponse.getInstallmentAmount()));
                Utilities.hideKeyboard(getActivity());
                final LankaBanglaDpsAmountInputFragment lankaBanglaDpsAmountInputFragment = new LankaBanglaDpsAmountInputFragment();

                if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(lankaBanglaDpsAmountInputFragment, bundle, 1, true);
                }
            }
        });
        billDetailsDialog.show();
    }
}
