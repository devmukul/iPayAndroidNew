package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.android.fragment.transaction.IPayTransactionHistoryFragment;
import bd.com.ipay.android.utility.TransactionHistoryType;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.BillParam;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.GetSavedBillResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.Notification;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayBillPayTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeBillAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.SaveBillDialog;

public class LankaBanglaDpsBillSuccessFragment extends IPayBillPayTransactionSuccessFragment implements HttpResponseListener {

	private Number billAmount;
	private String accountNumber;
	private String accountUserName;

	private String otherPersonName;
	private String otherPersonMobile;
	private HttpRequestPostAsyncTask linkThreeBillPayTask = null;
	protected AnimatedProgressDialog customProgressDialog;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			billAmount = (Number) getArguments().getSerializable(LankaBanglaDpsAmountInputFragment.INSTALLMENT_AMOUNT_KEY);
			accountNumber = getArguments().getString(LankaBanglaDpsAmountInputFragment.ACCOUNT_NUMBER_KEY, "");
			accountUserName = getArguments().getString(LankaBanglaDpsAmountInputFragment.ACCOUNT_USER_NAME_KEY, "");
			otherPersonName = getArguments().getString(LankaBanglaDpsAmountInputFragment.OTHER_PERSON_NAME_KEY, "");
			otherPersonMobile = getArguments().getString(LankaBanglaDpsAmountInputFragment.OTHER_PERSON_MOBILE_KEY, "");
		}

		customProgressDialog = new AnimatedProgressDialog(getActivity());
		customProgressDialog.setCancelable(false);
	}

	@Override
	protected void setupViewProperties() {
		setTransactionSuccessMessage(getStyledTransactionDescription(R.string.pay_bill_success_message, billAmount));
		setSuccessDescription(getString(R.string.pay_bill_success_description));
		setName(accountNumber);
		setUserName(accountUserName);
		setReceiverImage(R.drawable.ic_lankabd2);
	}

	@Override
	protected void performContinueAction() {
		showSaveInfoDialogue(accountNumber);
	}

	private void showSaveInfoDialogue(final String subscribeId) {
		if (getActivity() == null)
			return;

		final SaveBillDialog saveBillDialog = new SaveBillDialog(getContext());
		saveBillDialog.setTitle(getString(R.string.save_bill_info));
		saveBillDialog.setAccountInfo(accountNumber, getString(R.string.account_number));

		saveBillDialog.setCloseButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Utilities.hideKeyboard(getContext(), v);
				saveBillDialog.cancel();
			}
		});
		saveBillDialog.setPayBillButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Utilities.hideKeyboard(getContext(), v);
				saveBillDialog.cancel();
				saveAndSchedule(saveBillDialog.getBillTitleInfo(), saveBillDialog.getIsScheduled(), saveBillDialog.getScheduledateInfo() );

			}
		});
		saveBillDialog.show();
	}

	protected void saveAndSchedule(String shortname, boolean isSchedule, String date) {
		if (!Utilities.isConnectionAvailable(getContext())) {
			Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
		}
		if (linkThreeBillPayTask == null) {

			Date c = new Date();
			SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String formattedDate = curFormater.format(c);
			SavedBill savedBill = new SavedBill();
			bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData metaData = null;
			if(!TextUtils.isEmpty(otherPersonName) && ! TextUtils.isEmpty(otherPersonMobile)){
				metaData = new MetaData(new Notification(otherPersonName, otherPersonMobile));
				savedBill.setPaidForOthers(true);
			}else{
				savedBill.setPaidForOthers(false);
			}

			List<BillParam> billParamList = new ArrayList<>();
			billParamList.add(new BillParam(getString(R.string.account_number), "accountNumber", accountNumber, "String"));
			billParamList.add(new BillParam("Amount", "amount", String.valueOf(billAmount), "Number"));

			savedBill.setShortName(shortname);
			if(isSchedule) {
				savedBill.setIsScheduledToo(true);
				savedBill.setDateOfBillPayment(Integer.valueOf(date));
				savedBill.setSkipNumberOfMonths(0);
			}else {
				savedBill.setIsScheduledToo(false);
				savedBill.setDateOfBillPayment(1);
				savedBill.setSkipNumberOfMonths(0);
			}
			savedBill.setProviderCode("LANKABANGLA-DPS");
			savedBill.setLastPaid(formattedDate);
			savedBill.setBillParams(billParamList);
			savedBill.setMetaData(metaData);

			String json = new Gson().toJson(savedBill);
			String uri = Constants.BASE_URL_UTILITY + Constants.URL_SAVED_BILL;
			linkThreeBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LINK_THREE_BILL_PAY,
					uri, json, getActivity(), this, false);
			linkThreeBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			customProgressDialog.showDialog();
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
            customProgressDialog.dismissDialog();
            linkThreeBillPayTask = null;
        } else {
            try {
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_LINK_THREE_BILL_PAY:
                        GetSavedBillResponse deleteBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);

                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                customProgressDialog.setTitle(R.string.success);
                                customProgressDialog.showSuccessAnimationAndMessage(deleteBillResponse.getMessage());

                                if (getContext() != null) {
                                    Intent broadcastIntent = new Intent();
                                    Intent intent = new Intent();
                                    intent.setAction(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_UPDATE_ACTION);
                                    intent.putExtra(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_TYPE_KEY,
                                            TransactionHistoryType.COMPLETED);
                                    LocalBroadcastManager.getInstance(getContext())
                                            .sendBroadcast(broadcastIntent);
                                }

                                if (getActivity() != null) {
                                    getActivity().setResult(Activity.RESULT_OK);
                                    getActivity().finish();
                                }
                                break;
                            default:
                                if (getActivity() != null) {
                                    customProgressDialog.showFailureAnimationAndMessage(deleteBillResponse.getMessage());
                                    break;
                                }
                                break;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                customProgressDialog.showFailureAnimationAndMessage(getString(R.string.failed));
            }
            linkThreeBillPayTask = null;
        }


    }
}
