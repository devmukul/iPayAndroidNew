package bd.com.ipay.ipayskeleton.Model.MMModule.TransactionHistory;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TransactionHistoryClass implements Parcelable {

    private final String originatingMobileNumber;
    private final String receiverInfo;
    private final double amount;
    private final double fee;
    private final double netAmount;
    private final double balance;
    private final int serviceID;
    private final int statusCode;
    private final String purpose;
    private final String statusDescription;
    private final String description;
    private final String transactionID;
    private final long time;
    private final long requestTime;
    private final long responseTime;
    private final long id;
    private final TransactionHistoryAdditionalInfo additionalInfo;

    public String getOriginatingMobileNumber() {
        return originatingMobileNumber;
    }

    public double getAmount() {
        return amount;
    }

    public double getFee() {
        return fee;
    }

    /**
     * @return The name of the receiver if found, otherwise the mobile number. For add money/withdraw
     * money, returns the bank name.
     */
    public String getReceiver() {
        if (serviceID != Constants.TRANSACTION_HISTORY_OPENING_BALANCE && originatingMobileNumber == null)
            return "";

        try {
            switch (serviceID) {
                case (Constants.TRANSACTION_HISTORY_TOP_UP_ROLLBACK):
                    return receiverInfo;
                case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY_ROLL_BACK):
                    return getBankName();
                case (Constants.TRANSACTION_HISTORY_OPENING_BALANCE):
                    return "iPay";
                case (Constants.TRANSACTION_HISTORY_SEND_MONEY):
                    if (additionalInfo != null)
                        return additionalInfo.getUserName();
                    else
                        return "";
                case (Constants.TRANSACTION_HISTORY_ADD_MONEY):
                    return getBankName();
                case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY):
                    return getBankName();
                case (Constants.TRANSACTION_HISTORY_TOP_UP):
                    return receiverInfo;
                case (Constants.TRANSACTION_HISTORY_REQUEST_FOR_PAYMENT):
                case (Constants.TRANSACTION_HISTORY_MAKE_PAYMENT):
                    if (additionalInfo != null)
                        return additionalInfo.getUserName();
                    else
                        return "";
                case Constants.TRANSACTION_HISTORY_EDUCATION:
                    return additionalInfo.getUserName();
                case Constants.TRANSACTION_HISTORY_REQUEST_MONEY:
                    if (additionalInfo != null)
                        return additionalInfo.getUserName();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getNetAmountFormatted(String userMobileNumber) {
        if (serviceID != Constants.TRANSACTION_HISTORY_OPENING_BALANCE && originatingMobileNumber == null || receiverInfo == null)
            return Utilities.formatTakaWithComma(netAmount);

        /* **** Service charge effect ****
          In case of send money with service charge, the amount should be shown, instead of net amount.
          For example, A sends 10 tk to B with service charge 2 tk. B receives tk 8 while A sends tk 10.
          A should see 10 tk in the history while B sees 8

          Same thing will be applicable in withdraw money, top up and all kinds of payment
        */

        switch (serviceID) {
            case (Constants.TRANSACTION_HISTORY_TOP_UP_ROLLBACK):
                return "+" + Utilities.formatTakaWithComma(netAmount);
            case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY_ROLL_BACK):
                return "+" + Utilities.formatTakaWithComma(netAmount);
            case (Constants.TRANSACTION_HISTORY_OPENING_BALANCE):
                return "+" + Utilities.formatTakaWithComma(netAmount);
            case (Constants.TRANSACTION_HISTORY_SEND_MONEY):
                if (originatingMobileNumber.equals(userMobileNumber)) {
                    return "+" + Utilities.formatTakaWithComma(netAmount);
                } else if (receiverInfo.equals(userMobileNumber)) {
                    // Service charge effect
                    return "-" + Utilities.formatTakaWithComma(amount);
                }
            case (Constants.TRANSACTION_HISTORY_ADD_MONEY):
                return "+" + Utilities.formatTakaWithComma(netAmount);
            case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY):
                return "-" + Utilities.formatTakaWithComma(amount); // Service charge effect
            case (Constants.TRANSACTION_HISTORY_TOP_UP):
                return "-" + Utilities.formatTakaWithComma(amount); // Service charge effect
            case (Constants.TRANSACTION_HISTORY_MAKE_PAYMENT):
                if (originatingMobileNumber.equals(userMobileNumber)) {
                    return "+" + Utilities.formatTakaWithComma(netAmount);
                } else if (receiverInfo.equals(userMobileNumber)) {
                    return "-" + Utilities.formatTakaWithComma(amount); // Service charge effect
                }
            case (Constants.TRANSACTION_HISTORY_EDUCATION):
                return "-" + Utilities.formatTakaWithComma(amount); // Service charge effect
        }

        return Utilities.formatTakaWithComma(netAmount);
    }

    public double getNetAmount() {
        return netAmount;
    }

    public double getBalance() {
        return balance;
    }

    public long getId() {
        return id;
    }

    public int getServiceID() {
        return serviceID;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public String getDescription() {
        return description;
    }

    public long getTime() {
        return time;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public TransactionHistoryAdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }

    public String getReceiverInfo() {
        return receiverInfo;
    }

    public String getDescription(String userMobileNumber) {
        try {
            if (serviceID != Constants.TRANSACTION_HISTORY_OPENING_BALANCE && originatingMobileNumber == null)
                return "No information available";

            switch (serviceID) {
                case (Constants.TRANSACTION_HISTORY_TOP_UP_ROLLBACK):
                    return "Top up failed and returned " + Utilities.formatTaka(getNetAmount());
                case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY_ROLL_BACK):
                    return "Withdraw money failed and returned " + Utilities.formatTaka(getNetAmount());
                case (Constants.TRANSACTION_HISTORY_OPENING_BALANCE):
                    return "Opening balance from iPay";
                case (Constants.TRANSACTION_HISTORY_SEND_MONEY):
                    if (originatingMobileNumber.equals(userMobileNumber)) {
                        if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                            return "Sent " + Utilities.formatTaka(getNetAmount()) + " to " + additionalInfo.getUserName();
                        else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                            return "Sending " + Utilities.formatTaka(getNetAmount()) + " to " + additionalInfo.getUserName();
                        else {
                            if (additionalInfo.getUserName() == null)
                                return "Failed to send money";
                            else
                                return "Failed to send " + Utilities.formatTaka(getNetAmount()) + " to " + additionalInfo.getUserName();
                        }
                    } else if (receiverInfo.equals(userMobileNumber)) {
                        if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                            return "Received " + Utilities.formatTaka(getNetAmount()) + " from " + additionalInfo.getUserName();
                        else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                            return "Receiving " + Utilities.formatTaka(getNetAmount()) + " from " + additionalInfo.getUserName();
                        else {
                            if (additionalInfo.getUserName() == null)
                                return "Failed to receive money";
                            else
                                return "Failed to receive " + Utilities.formatTaka(getNetAmount()) + " from " + additionalInfo.getUserName();
                        }
                    } else {
                        return "No information available";
                    }
                case (Constants.TRANSACTION_HISTORY_ADD_MONEY):
                    if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                        return "Added " + Utilities.formatTaka(getNetAmount()) + "  from account " + getBankAccountNumber() + ", " + getBankName() + "(" + getBankBranch() + ")";
                    else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                        return "Adding " + Utilities.formatTaka(getNetAmount()) + "  from account " + getBankAccountNumber() + ", " + getBankName() + "(" + getBankBranch() + ")";
                    else
                        return "Failed to add " + Utilities.formatTaka(getNetAmount()) + " from account " + getBankAccountNumber() + ", " + getBankName() + "(" + getBankBranch() + ")";
                case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY):
                    if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                        return "Transferred " + Utilities.formatTaka(getNetAmount()) + " to account " + getBankAccountNumber() + ", " + getBankName() + "(" + getBankBranch() + ")";
                    else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                        return "Transferred " + Utilities.formatTaka(getNetAmount()) + " to account " + getBankAccountNumber() + ", " + getBankName() + "(" + getBankBranch() + ")";
                    else
                        return "Failed to transfer " + Utilities.formatTaka(getNetAmount()) + " to account " + getBankAccountNumber() + ", " + getBankName() + "(" + getBankBranch() + ")";
                case (Constants.TRANSACTION_HISTORY_TOP_UP):
                    if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                        return "Mobile TopUp of " + Utilities.formatTaka(getNetAmount()) + " to " + receiverInfo;
                    else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                        return "Mobile TopUp of " + Utilities.formatTaka(getNetAmount()) + " to " + receiverInfo;
                    else
                        return "Mobile TopUp of " + Utilities.formatTaka(getNetAmount()) + " to " + receiverInfo + " failed ";
                case (Constants.TRANSACTION_HISTORY_REQUEST_FOR_PAYMENT):
                case (Constants.TRANSACTION_HISTORY_MAKE_PAYMENT):
                    if (originatingMobileNumber.equals(userMobileNumber)) {
                        if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                            return "Payment of " + Utilities.formatTaka(getNetAmount()) + " sent to " + additionalInfo.getUserName();
                        else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                            return "Sending payment of " + Utilities.formatTaka(getNetAmount()) + " to " + additionalInfo.getUserName();
                        else {
                            if (additionalInfo.getUserName() == null)
                                return "Payment sending failed";
                            else
                                return "Sending payment of " + Utilities.formatTaka(getNetAmount()) + "  to " + additionalInfo.getUserName() + " failed";
                        }
                    } else if (receiverInfo.equals(userMobileNumber)) {
                        if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                            return "Payment of " + Utilities.formatTaka(getNetAmount()) + " received from " + additionalInfo.getUserName();
                        else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                            return "Receiving payment of " + Utilities.formatTaka(getNetAmount()) + " from " + additionalInfo.getUserName();
                        else {
                            if (additionalInfo.getUserName() == null)
                                return "Payment receiving failed ";
                            else
                                return "Receiving payment of " + Utilities.formatTaka(getNetAmount()) + "  from " + additionalInfo.getUserName() + " failed ";
                        }
                    } else
                        return "No information available";
                case (Constants.TRANSACTION_HISTORY_EDUCATION):
                    if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED)
                        return "Education payment of " + Utilities.formatTaka(getNetAmount()) + " for " + additionalInfo.getUserName();
                    else if (statusCode == Constants.TRANSACTION_STATUS_PROCESSING)
                        return "Education payment of " + Utilities.formatTaka(getNetAmount()) + " for " + additionalInfo.getUserName();
                    else
                        return "Education payment of " + Utilities.formatTaka(getNetAmount()) + " for " + additionalInfo.getUserName() + " failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "No information available";
    }

    public String getShortDescription(String userMobileNumber) {
        try {
            if (serviceID != Constants.TRANSACTION_HISTORY_OPENING_BALANCE && originatingMobileNumber == null)
                return "No Information Available";

            switch (serviceID) {
                case (Constants.TRANSACTION_HISTORY_TOP_UP_ROLLBACK):
                    return "TopUp Rollback";
                case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY_ROLL_BACK):
                    return "Withdraw Money Rollback";
                case (Constants.TRANSACTION_HISTORY_OPENING_BALANCE):
                    return "Opening Balance";
                case (Constants.TRANSACTION_HISTORY_SEND_MONEY):
                    if (originatingMobileNumber.equals(userMobileNumber)) {
                        return "Money Sent";
                    } else if (receiverInfo.equals(userMobileNumber)) {
                        return "Money Received";
                    } else {
                        return "No Information Available";
                    }
                case (Constants.TRANSACTION_HISTORY_ADD_MONEY):
                    return "Money Added";
                case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY):
                    return "Money Withdrawn";
                case (Constants.TRANSACTION_HISTORY_TOP_UP):
                    return "Mobile TopUp";
                case (Constants.TRANSACTION_HISTORY_REQUEST_FOR_PAYMENT):
                    if (statusCode == Constants.TRANSACTION_STATUS_ACCEPTED) {
                        if (originatingMobileNumber.equals(userMobileNumber)) {
                            return "Payment Request Sent";
                        } else if (receiverInfo.equals(userMobileNumber)) {
                            return "Payment Request Received";
                        } else {
                            return "No Information Available";
                        }
                    } else {
                        if (originatingMobileNumber.equals(userMobileNumber)) {
                            return "Payment Request Sent";
                        } else if (receiverInfo.equals(userMobileNumber)) {
                            return "Payment Request Received";
                        } else {
                            return "No Information Available";
                        }
                    }
                case (Constants.TRANSACTION_HISTORY_MAKE_PAYMENT):
                    if (originatingMobileNumber.equals(userMobileNumber)) {
                        return "Payment Made";
                    } else if (receiverInfo.equals(userMobileNumber)) {
                        return "Payment Received";
                    } else {
                        return "No Information Available";
                    }
                case (Constants.TRANSACTION_HISTORY_EDUCATION):
                    return "Education Payment";
                case (Constants.TRANSACTION_HISTORY_REQUEST_MONEY):
                    if (originatingMobileNumber.equals(userMobileNumber)) {
                        return "Money Request Sent";
                    } else if (receiverInfo.equals(userMobileNumber)) {
                        return "Money Request Received";
                    } else {
                        return "No Information Available";
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "No Information Available";
    }

    private String getBankAccountNumber() {
        if (additionalInfo.getBankAccountNumber() == null)
            return "Bank";
        else
            return additionalInfo.getBankAccountNumber();
    }

    private String getBankName() {
        if (additionalInfo.getBankName() == null)
            return "Bank";
        else
            return additionalInfo.getBankName();
    }

    private String getBankBranch() {
        if (additionalInfo.getBranchName() == null)
            return "Bank";
        else
            return additionalInfo.getBranchName();
    }

    public double getAmount(String userMobileNumber) {
        if (serviceID != Constants.TRANSACTION_HISTORY_OPENING_BALANCE && (
                originatingMobileNumber == null || receiverInfo == null))
            return 0;

        switch (serviceID) {
            case (Constants.TRANSACTION_HISTORY_OPENING_BALANCE):
                return +netAmount;
            case (Constants.TRANSACTION_HISTORY_SEND_MONEY):
                if (originatingMobileNumber.equals(userMobileNumber))
                    return -netAmount;
                else if (receiverInfo.equals(userMobileNumber))
                    return +netAmount;
                else
                    return 0;
            case (Constants.TRANSACTION_HISTORY_ADD_MONEY):
                return +netAmount;
            case (Constants.TRANSACTION_HISTORY_WITHDRAW_MONEY):
                return -netAmount;
            case (Constants.TRANSACTION_HISTORY_TOP_UP):
                return -netAmount;
            case (Constants.TRANSACTION_HISTORY_REQUEST_FOR_PAYMENT):
            case (Constants.TRANSACTION_HISTORY_MAKE_PAYMENT):
                if (originatingMobileNumber.equals(userMobileNumber))
                    return -netAmount;
                else if (receiverInfo.equals(userMobileNumber))
                    return +netAmount;
                else
                    return 0;
        }

        return 0;
    }

    @Override
    public String toString() {
        return "TransactionHistoryClass{" +
                "originatingMobileNumber='" + originatingMobileNumber + '\'' +
                ", receiverInfo='" + receiverInfo + '\'' +
                ", amount=" + amount +
                ", fee=" + fee +
                ", netAmount=" + netAmount +
                ", balance=" + balance +
                ", serviceID=" + serviceID +
                ", statusCode=" + statusCode +
                ", purpose='" + purpose + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                ", description='" + description + '\'' +
                ", transactionID='" + transactionID + '\'' +
                ", time=" + time +
                ", requestTime=" + requestTime +
                ", responseTime=" + responseTime +
                ", additionalInfo=" + additionalInfo +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.originatingMobileNumber);
        dest.writeString(this.receiverInfo);
        dest.writeDouble(this.amount);
        dest.writeDouble(this.fee);
        dest.writeDouble(this.netAmount);
        dest.writeDouble(this.balance);
        dest.writeInt(this.serviceID);
        dest.writeInt(this.statusCode);
        dest.writeString(this.purpose);
        dest.writeString(this.statusDescription);
        dest.writeString(this.description);
        dest.writeString(this.transactionID);
        dest.writeLong(this.time);
        dest.writeLong(this.requestTime);
        dest.writeLong(this.responseTime);
        dest.writeLong(this.id);
        dest.writeParcelable(this.additionalInfo, flags);
    }

    protected TransactionHistoryClass(Parcel in) {
        this.originatingMobileNumber = in.readString();
        this.receiverInfo = in.readString();
        this.amount = in.readDouble();
        this.fee = in.readDouble();
        this.netAmount = in.readDouble();
        this.balance = in.readDouble();
        this.serviceID = in.readInt();
        this.statusCode = in.readInt();
        this.purpose = in.readString();
        this.statusDescription = in.readString();
        this.description = in.readString();
        this.transactionID = in.readString();
        this.time = in.readLong();
        this.requestTime = in.readLong();
        this.responseTime = in.readLong();
        this.id = in.readLong();
        this.additionalInfo = in.readParcelable(TransactionHistoryAdditionalInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<TransactionHistoryClass> CREATOR = new Parcelable.Creator<TransactionHistoryClass>() {
        @Override
        public TransactionHistoryClass createFromParcel(Parcel source) {
            return new TransactionHistoryClass(source);
        }

        @Override
        public TransactionHistoryClass[] newArray(int size) {
            return new TransactionHistoryClass[size];
        }
    };
}
