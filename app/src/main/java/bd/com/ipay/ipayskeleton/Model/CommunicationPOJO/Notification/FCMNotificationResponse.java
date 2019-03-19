package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;

public class FCMNotificationResponse {
    private int serviceId;
    private boolean isReceiver;
    private String changed_data;
    private String icon;
    private long time;
    private String deepLink;
    private String transactionDetailsString;
    private String  transactionActivity;
    private TransactionHistory transactionHistory;

    public TransactionHistory getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(TransactionHistory transactionHistory) {
        this.transactionHistory = transactionHistory;
    }

    public String getTransactionActivity() {
        return transactionActivity;
    }

    public void setTransactionActivity(String  transactionActivity) {
        this.transactionActivity = transactionActivity;
    }

    public String getTransactionDetailsString() {
        return transactionDetailsString;
    }

    public void setTransactionDetailsString(String transactionDetailsString) {
        this.transactionDetailsString = transactionDetailsString;
    }

    public int getServiceId() {
        return serviceId;
    }

    public boolean isReceiver() {
        return isReceiver;
    }

    public String getIcon() {
        return icon;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public FCMNotificationData getNotificationData() {
        FCMNotificationData fcmNotificationData;
        Gson gson = new Gson();
        fcmNotificationData = gson.fromJson(changed_data, FCMNotificationData.class);
        return fcmNotificationData;
    }

    public long getTime() {
        return time;
    }
}
