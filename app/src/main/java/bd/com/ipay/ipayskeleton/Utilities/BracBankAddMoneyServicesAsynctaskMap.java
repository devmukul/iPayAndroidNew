package bd.com.ipay.ipayskeleton.Utilities;

import android.content.Context;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByBankInstantlyRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByBankRequestV3;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByBracBank;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.WithdrawMoneyRequestV3;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.SetPinRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentRequestByDeepLink;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.SendNewPaymentRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets.PurchaseTicketRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SendMoney.SendMoneyRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TopUp.TopupRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TwoFA.TwoFactorAuthServicesListWithOTPRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.AmberITBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.BanglalionBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.BrilliantBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.CarnivalBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DpdcBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaCardBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LinkThreeBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.WestZoneBillPayRequest;


public class BracBankAddMoneyServicesAsynctaskMap {
    private static HttpRequestPostAsyncTask mHttpPostAsyncTask;

    public static HttpRequestPostAsyncTask getPostAsyncTask(String command, String json,
                                                            String otp, Context context, String uri) {
        Gson gson = new Gson();
        switch (command) {
            case Constants.COMMAND_ADD_MONEY_FROM_BANK:
                AddMoneyByBracBank addMoneyByBracBank = new AddMoneyByBracBank(json, otp);
                String jsons = gson.toJson(addMoneyByBracBank);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_BANK, uri, jsons, context, false);
                return mHttpPostAsyncTask;
            default:
                return null;
        }
    }
}
