package bd.com.ipay.ipayskeleton.Utilities;

import android.content.Context;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByBankInstantlyRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByBankRequestV3;
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


public class TwoFactorAuthServicesAsynctaskMap {
    private static HttpRequestPostAsyncTask mHttpPostAsyncTask;
    private static HttpRequestPutAsyncTask mHttpPutAsyncTask;

    public static HttpRequestPutAsyncTask getPutAsyncTask(String command, String json,
                                                          String otp, Context context, String uri) {
        Gson gson = new Gson();
        switch (command) {
            case Constants.COMMAND_PUT_TWO_FACTOR_AUTH_SETTINGS:
                TwoFactorAuthServicesListWithOTPRequest twoFactorAuthServicesListWithOTPRequest = gson.fromJson(json,
                        TwoFactorAuthServicesListWithOTPRequest.class);
                if (otp != null)
                    twoFactorAuthServicesListWithOTPRequest.setOtp(otp);
                json = gson.toJson(twoFactorAuthServicesListWithOTPRequest);
                mHttpPutAsyncTask = new HttpRequestPutAsyncTask(command, uri, json, context, false);
                return mHttpPutAsyncTask;

            case Constants.COMMAND_SET_PIN:
                SetPinRequest mSetPinRequest = gson.fromJson(json, SetPinRequest.class);
                if (otp != null)
                    mSetPinRequest.setOtp(otp);
                json = gson.toJson(mSetPinRequest);
                mHttpPutAsyncTask = new HttpRequestPutAsyncTask(Constants.COMMAND_SET_PIN, uri, json, context, false);
                return mHttpPutAsyncTask;
            default:
                return null;
        }
    }

    public static HttpRequestPostAsyncTask getPostAsyncTask(String command, String json,
                                                            String otp, Context context, String uri) {
        Gson gson = new Gson();
        String pin;
        switch (command) {

            case Constants.COMMAND_SEND_MONEY:
                SendMoneyRequest sendMoneyRequest = gson.fromJson(json, SendMoneyRequest.class);
                pin = sendMoneyRequest.getPin();
                json = gson.toJson(sendMoneyRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SEND_MONEY, uri, json, context, false);
                if (pin != null) {
                    mHttpPostAsyncTask.setPinAsHeader(pin);
                }
                if (otp != null) {
                    mHttpPostAsyncTask.setOtpAsHeader(otp);
                }
                return mHttpPostAsyncTask;

            case Constants.COMMAND_PAYMENT:
                PaymentRequest paymentRequest = gson.fromJson(json, PaymentRequest.class);
                pin = paymentRequest.getPin();
                json = gson.toJson(paymentRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_PAYMENT, uri, json, context, false);
                if (pin != null) {
                    mHttpPostAsyncTask.setPinAsHeader(pin);
                }
                if (otp != null) {
                    mHttpPostAsyncTask.setOtpAsHeader(otp);
                }
                return mHttpPostAsyncTask;

            case Constants.COMMAND_TOPUP_REQUEST:
                TopupRequest topupRequest = gson.fromJson(json, TopupRequest.class);
                pin = topupRequest.getPin();
                json = gson.toJson(topupRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_TOPUP_REQUEST, uri, json, context, false);
                if (pin != null) {
                    mHttpPostAsyncTask.setPinAsHeader(pin);
                }
                if (otp != null) {
                    mHttpPostAsyncTask.setOtpAsHeader(otp);
                }
                return mHttpPostAsyncTask;

            case Constants.COMMAND_ADD_MONEY_FROM_BANK:
                AddMoneyByBankRequestV3 addMoneyByBankRequest = gson.fromJson(json, AddMoneyByBankRequestV3.class);
                json = gson.toJson(addMoneyByBankRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_BANK, uri, json, context, false);
                return mHttpPostAsyncTask;
            case Constants.COMMAND_ADD_MONEY_FROM_BANK_INSTANTLY:
                AddMoneyByBankInstantlyRequest addMoneyByBankInstantlyRequest = gson.fromJson(json, AddMoneyByBankInstantlyRequest.class);
                if (otp != null)
                    addMoneyByBankInstantlyRequest.setOtp(otp);
                json = gson.toJson(addMoneyByBankInstantlyRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_BANK_INSTANTLY, uri, json, context, false);
                return mHttpPostAsyncTask;
            case Constants.COMMAND_WITHDRAW_MONEY:
                WithdrawMoneyRequestV3 withdrawMoneyRequest = gson.fromJson(json, WithdrawMoneyRequestV3.class);
                json = gson.toJson(withdrawMoneyRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_WITHDRAW_MONEY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_PAYMENT_BY_DEEP_LINK:
                PaymentRequestByDeepLink paymentRequestByDeepLink = gson.fromJson(json, PaymentRequestByDeepLink.class);
                if (otp != null) {
                    paymentRequestByDeepLink.setOtp(otp);
                }
                json = gson.toJson(paymentRequestByDeepLink);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_PAYMENT_BY_DEEP_LINK, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_SEND_PAYMENT_REQUEST:
                SendNewPaymentRequest mSendNewPaymentRequest = gson.fromJson(json, SendNewPaymentRequest.class);
                if (otp != null)
                    mSendNewPaymentRequest.setOtp(otp);
                json = gson.toJson(mSendNewPaymentRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SEND_PAYMENT_REQUEST, uri, json, context, false);
                return mHttpPostAsyncTask;
            case Constants.COMMAND_ACCEPT_REQUESTS_MONEY:
                RequestMoneyAcceptRejectOrCancelRequest requestMoneyAcceptRejectOrCancelRequest
                        = gson.fromJson(json, RequestMoneyAcceptRejectOrCancelRequest.class);
                if (otp != null)
                    requestMoneyAcceptRejectOrCancelRequest.setOtp(otp);
                json = gson.toJson(requestMoneyAcceptRejectOrCancelRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ACCEPT_REQUESTS_MONEY, uri, json, context, false);
                return mHttpPostAsyncTask;
            case Constants.COMMAND_ACCEPT_PAYMENT_REQUEST:
                RequestMoneyAcceptRejectOrCancelRequest mRequestMoneyAcceptRejectOrCancelRequest =
                        gson.fromJson(json, RequestMoneyAcceptRejectOrCancelRequest.class);
                if (otp != null)
                    mRequestMoneyAcceptRejectOrCancelRequest.setOtp(otp);
                json = gson.toJson(mRequestMoneyAcceptRejectOrCancelRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ACCEPT_PAYMENT_REQUEST, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_BANGLALION_BILL_PAY:
                BanglalionBillPayRequest billPayRequest = gson.fromJson(json, BanglalionBillPayRequest.class);
                if (otp != null)
                    billPayRequest.setOtp(otp);
                json = gson.toJson(billPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_BANGLALION_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_RAILWAY_TICKET_PURCHASE:
                PurchaseTicketRequest billPayRequest1 = gson.fromJson(json, PurchaseTicketRequest.class);
                if (otp != null)
                    billPayRequest1.setOtp(otp);
                json = gson.toJson(billPayRequest1);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_RAILWAY_TICKET_PURCHASE, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_DPDC_BILL_PAY:
                DpdcBillPayRequest dpdcBillPayRequest = gson.fromJson(json, DpdcBillPayRequest.class);
                if (otp != null)
                    dpdcBillPayRequest.setOtp(otp);
                json = gson.toJson(dpdcBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_DPDC_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_LINK_THREE_BILL_PAY:
                LinkThreeBillPayRequest linkThreeBillPayRequest = gson.fromJson(json, LinkThreeBillPayRequest.class);
                if (otp != null)
                    linkThreeBillPayRequest.setOtp(otp);
                json = gson.toJson(linkThreeBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LINK_THREE_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_AMBERIT_BILL_PAY:
                AmberITBillPayRequest amberITBillPayRequest = gson.fromJson(json, AmberITBillPayRequest.class);
                if (otp != null)
                    amberITBillPayRequest.setOtp(otp);
                json = gson.toJson(amberITBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_AMBERIT_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_WEST_ZONE_BILL_PAY:
                WestZoneBillPayRequest westZoneBillPayRequest = gson.fromJson(json, WestZoneBillPayRequest.class);
                if (otp != null)
                    westZoneBillPayRequest.setOtp(otp);
                json = gson.toJson(westZoneBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_WEST_ZONE_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_CARNIVAL_BILL_PAY:
                CarnivalBillPayRequest carnivalBillPayRequest = gson.fromJson(json, CarnivalBillPayRequest.class);
                if (otp != null)
                    carnivalBillPayRequest.setOtp(otp);
                json = gson.toJson(carnivalBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CARNIVAL_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_DESCO_BILL_PAY:
                DescoBillPayRequest descoBillPayRequest = gson.fromJson(json, DescoBillPayRequest.class);
                if (otp != null)
                    descoBillPayRequest.setOtp(otp);
                json = gson.toJson(descoBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_DESCO_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_BRILLIANT_RECHARGE:
                BrilliantBillPayRequest brilliantBillPayRequest = gson.fromJson(json, BrilliantBillPayRequest.class);
                if (otp != null)
                    brilliantBillPayRequest.setOtp(otp);
                json = gson.toJson(brilliantBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_BRILLIANT_RECHARGE, uri, json, context, false);
                return mHttpPostAsyncTask;

            case Constants.COMMAND_LANKABANGLA_BILL_PAY:
                LankaBanglaCardBillPayRequest lankaBanglaCardBillPayRequest = gson.fromJson(json, LankaBanglaCardBillPayRequest.class);
                if (otp != null)
                    lankaBanglaCardBillPayRequest.setOtp(otp);
                json = gson.toJson(lankaBanglaCardBillPayRequest);
                mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LANKABANGLA_BILL_PAY, uri, json, context, false);
                return mHttpPostAsyncTask;
            default:
                return null;
        }
    }
}
