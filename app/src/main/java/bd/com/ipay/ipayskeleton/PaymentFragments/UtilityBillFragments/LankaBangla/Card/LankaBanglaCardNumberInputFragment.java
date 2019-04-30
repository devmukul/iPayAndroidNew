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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractCardNumberInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps.LankaBanglaDpsAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BillDetailsDialog;

public class LankaBanglaCardNumberInputFragment extends IPayAbstractCardNumberInputFragment implements HttpResponseListener {

	private HttpRequestGetAsyncTask mGetLankaBanglaCardUserInfoAsyncTask = null;
	private final Gson gson = new GsonBuilder().create();
	private AnimatedProgressDialog mProgressDialog;


    private String userId;
    private String amount;
    private String amountType;
    private boolean isFromSaved;
    private String metaDataText;
    private String cardType;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null){
            isFromSaved = getArguments().getBoolean("IS_FROM_HISTORY", false);
            userId = getArguments().getString(Constants.ACCOUNT_ID);
            amount = getArguments().getString(Constants.AMOUNT);
            amountType = getArguments().getString(Constants.AMOUNT_TYPE);
            metaDataText =  getArguments().getString("META_DATA");
        }

        if (getActivity() != null)
            getActivity().setTitle(R.string.credit_card_bill_title);

		setCardIconImageResource(R.drawable.ic_debit_credit_card_icon);
		setMessage(getString(R.string.lanka_bangla_card_number_input_message));
		setCardNumberHint(getString(R.string.lanka_bangla_card_number));
		setAllowedCards(CardNumberValidator.Cards.VISA, CardNumberValidator.Cards.MASTERCARD);
		mProgressDialog = new AnimatedProgressDialog(getActivity());

        if(isFromSaved && !TextUtils.isEmpty(userId)){
            setCardNumber(userId);
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
        if (TextUtils.isEmpty(getCardNumber())) {
            showErrorMessage(getString(R.string.empty_card_number_message));
            return false;
        } else if (!CardNumberValidator.validateCardNumber(getCardNumber(), getAllowedCards())) {
            showErrorMessage(getString(R.string.invalid_card_number_message));
            return false;
        } else if (ifPayingForOtherPerson()) {
            String mobileNumber = ProfileInfoCacheManager.getMobileNumber();
            System.out.println("mobileNumber  "+mobileNumber +" "+ContactEngine.formatMobileNumberBD(getOtherPersonMobile()));
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
            } else {
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
		} else if (mGetLankaBanglaCardUserInfoAsyncTask != null) {
			return;
		}
		CardNumberValidator.Cards cards = CardNumberValidator.getCardType(getCardNumber());

		System.out.println("Card Type "+cards);
		final String url;
		if (cards == null)
			return;
		switch (cards) {
			case VISA:
			    cardType = "LANKABANGLA-VISA";
				url = Constants.BASE_URL_UTILITY + Constants.URL_GET_LANKA_BANGLA_VISA_CUSTOMER_INFO + CardNumberValidator.sanitizeEntry(getCardNumber(), true);
				break;
			case MASTERCARD:


                cardType = "LANKABANGLA-MASTERCARD";
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
                bundle.putString(LankaBanglaAmountInputFragment.OTHER_PERSON_NAME_KEY, getOtherPersonName());
                bundle.putString(LankaBanglaAmountInputFragment.OTHER_PERSON_MOBILE_KEY, ContactEngine.formatMobileNumberBD(getOtherPersonMobile()) );
                bundle.putString(Constants.CARD_TYPE, cardType);

                if(isFromSaved) {
                    bundle.putBoolean("IS_FROM_HISTORY", true);
                    bundle.putString(Constants.AMOUNT, amount);
                    bundle.putString(Constants.AMOUNT_TYPE, amountType);
                }
                Utilities.hideKeyboard(getActivity());
                final LankaBanglaAmountInputFragment lankaBanglaAmountInputFragment = new LankaBanglaAmountInputFragment();

                if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
                    int maxBackStack=2;
                    if(isFromSaved)
                        maxBackStack =3;
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(lankaBanglaAmountInputFragment, bundle, maxBackStack, true);
                }
            }
        });
        billDetailsDialog.show();
    }
}
