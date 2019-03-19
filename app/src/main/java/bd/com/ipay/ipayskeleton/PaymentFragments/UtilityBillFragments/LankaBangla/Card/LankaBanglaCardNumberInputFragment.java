package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Card;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractCardNumberInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BillDetailsDialog;

public class LankaBanglaCardNumberInputFragment extends IPayAbstractCardNumberInputFragment implements HttpResponseListener {

	private HttpRequestGetAsyncTask mGetLankaBanglaCardUserInfoAsyncTask = null;
	private final Gson gson = new GsonBuilder().create();
	private AnimatedProgressDialog mProgressDialog;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null)
            getActivity().setTitle(R.string.credit_card_bill_title);

		setCardIconImageResource(R.drawable.ic_debit_credit_card_icon);
		setMessage(getString(R.string.lanka_bangla_card_number_input_message));
		setCardNumberHint(getString(R.string.lanka_bangla_card_number));
		setAllowedCards(CardNumberValidator.Cards.VISA, CardNumberValidator.Cards.MASTERCARD);
		mProgressDialog = new AnimatedProgressDialog(getActivity());
	}

    @Override
    protected boolean verifyInput() {
        if (TextUtils.isEmpty(getCardNumber())) {
            showErrorMessage(getString(R.string.empty_card_number_message));
            return false;
        } else if (!CardNumberValidator.validateCardNumber(getCardNumber(), getAllowedCards())) {
            showErrorMessage(getString(R.string.invalid_card_number_message));
            return false;
        } else {
            return true;
        }
    }

	@Override
	protected void performContinueAction() {
		if (!Utilities.isConnectionAvailable(getContext())) {
			Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
		} else if (mGetLankaBanglaCardUserInfoAsyncTask != null) {
			return;
		}
		CardNumberValidator.Cards cards = CardNumberValidator.getCardType(getCardNumber());
		final String url;
		if (cards == null)
			return;
		switch (cards) {
			case VISA:
				url = Constants.BASE_URL_UTILITY + Constants.URL_GET_LANKA_BANGLA_VISA_CUSTOMER_INFO + CardNumberValidator.sanitizeEntry(getCardNumber(), true);
				break;
			case MASTERCARD:
				url = Constants.BASE_URL_UTILITY + Constants.URL_GET_LANKA_BANGLA_MASTERCARD_CUSTOMER_INFO + CardNumberValidator.sanitizeEntry(getCardNumber(), true);
				break;
			default:
				return;
		}
		mGetLankaBanglaCardUserInfoAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_LANKA_BANGLA_CUSTOMER_INFO,
				url, getContext(), this, false);
		mGetLankaBanglaCardUserInfoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mProgressDialog.setTitle(R.string.please_wait_no_ellipsis);
		mProgressDialog.showDialog();
	}

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), mProgressDialog)) {
            mGetLankaBanglaCardUserInfoAsyncTask = null;
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
                mProgressDialog.dismissDialog();
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_GET_LANKA_BANGLA_CUSTOMER_INFO:
                        mGetLankaBanglaCardUserInfoAsyncTask = null;
                        LankaBanglaCustomerInfoResponse lankaBanglaCustomerInfoResponse = gson.fromJson(result.getJsonString(), LankaBanglaCustomerInfoResponse.class);
                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                showLankaBanglaUserInfo(lankaBanglaCustomerInfoResponse);
                                break;
                            default:
                                if (!TextUtils.isEmpty(lankaBanglaCustomerInfoResponse.getMessage())) {
                                    Utilities.showErrorDialog(getContext(), lankaBanglaCustomerInfoResponse.getMessage());
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
        mProgressDialog.dismissDialog();
    }

    private void showLankaBanglaUserInfo(final LankaBanglaCustomerInfoResponse lankaBanglaCustomerInfoResponse) {
        if (getActivity() == null)
            return;

        final BillDetailsDialog billDetailsDialog = new BillDetailsDialog(getContext());
        billDetailsDialog.setTitle(getString(R.string.bill_details));
        billDetailsDialog.setClientLogoImageResource(R.drawable.ic_lankabd2);
        CardNumberValidator.Cards cards = CardNumberValidator.getCardType(lankaBanglaCustomerInfoResponse.getCardNumber());
        if (cards != null)
            billDetailsDialog.setBillTitleInfo(CardNumberValidator.deSanitizeEntry(lankaBanglaCustomerInfoResponse.getCardNumber(), ' '), cards.getCardIconId());
        else
            billDetailsDialog.setBillTitleInfo(CardNumberValidator.deSanitizeEntry(lankaBanglaCustomerInfoResponse.getCardNumber(), ' '));
        billDetailsDialog.setBillSubTitleInfo(lankaBanglaCustomerInfoResponse.getName());

        billDetailsDialog.setTotalBillInfo(getString(R.string.total_outstanding).toUpperCase(), Integer.parseInt(lankaBanglaCustomerInfoResponse.getCreditBalance()));
        billDetailsDialog.setMinimumBillInfo(getString(R.string.minimum_pay).toUpperCase(), Integer.parseInt(lankaBanglaCustomerInfoResponse.getMinimumPay()));

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
                bundle.putInt(LankaBanglaAmountInputFragment.TOTAL_OUTSTANDING_AMOUNT_KEY, Integer.parseInt(lankaBanglaCustomerInfoResponse.getCreditBalance()));
                bundle.putInt(LankaBanglaAmountInputFragment.MINIMUM_PAY_AMOUNT_KEY, Integer.parseInt(lankaBanglaCustomerInfoResponse.getMinimumPay()));
                bundle.putString(LankaBanglaAmountInputFragment.CARD_NUMBER_KEY, lankaBanglaCustomerInfoResponse.getCardNumber());
                bundle.putString(LankaBanglaAmountInputFragment.CARD_USER_NAME_KEY, lankaBanglaCustomerInfoResponse.getName());
                Utilities.hideKeyboard(getActivity());
                final LankaBanglaAmountInputFragment lankaBanglaAmountInputFragment = new LankaBanglaAmountInputFragment();

                if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(lankaBanglaAmountInputFragment, bundle, 2, true);
                }
            }
        });
        billDetailsDialog.show();
    }
}
