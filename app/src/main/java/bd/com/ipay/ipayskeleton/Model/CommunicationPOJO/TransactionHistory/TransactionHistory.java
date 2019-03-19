package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class TransactionHistory implements Parcelable {
    private final long insertTime;
    private final String accountId;
    private final int serviceId;
    private final String transactionId;
    private final String message;
    private final double amount;
    private final double fee;
    private final double netAmount;
    private final String purpose;
    private final String description;
    private final String shortDesc;
    private final int statusCode;
    private final String statusInWord;
    private final String type;
    private final Double accountBalance;
    private final Double availableBalance;
    private final TransactionHistoryAdditionalInfo otherParty;
    private final String[] actions;
    private final String outletName;
    private final long outletId;
    private final TransactionMetaData metaData;


    protected TransactionHistory(Parcel in) {
        insertTime = in.readLong();
        accountId = in.readString();
        serviceId = in.readInt();
        transactionId = in.readString();
        message = in.readString();
        amount = in.readDouble();
        fee = in.readDouble();
        netAmount = in.readDouble();
        purpose = in.readString();
        description = in.readString();
        shortDesc = in.readString();
        statusCode = in.readInt();
        statusInWord = in.readString();
        type = in.readString();
        if (in.readByte() == 0) {
            accountBalance = null;
        } else {
            accountBalance = in.readDouble();
        }
        if (in.readByte() == 0) {
            availableBalance = null;
        } else {
            availableBalance = in.readDouble();
        }
        otherParty = in.readParcelable(TransactionHistoryAdditionalInfo.class.getClassLoader());
        actions = in.createStringArray();
        outletName = in.readString();
        outletId = in.readLong();
        metaData = in.readParcelable(TransactionMetaData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(insertTime);
        dest.writeString(accountId);
        dest.writeInt(serviceId);
        dest.writeString(transactionId);
        dest.writeString(message);
        dest.writeDouble(amount);
        dest.writeDouble(fee);
        dest.writeDouble(netAmount);
        dest.writeString(purpose);
        dest.writeString(description);
        dest.writeString(shortDesc);
        dest.writeInt(statusCode);
        dest.writeString(statusInWord);
        dest.writeString(type);
        if (accountBalance == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(accountBalance);
        }
        if (availableBalance == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(availableBalance);
        }
        dest.writeParcelable(otherParty, flags);
        dest.writeStringArray(actions);
        dest.writeString(outletName);
        dest.writeLong(outletId);
        dest.writeParcelable(metaData, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransactionHistory> CREATOR = new Creator<TransactionHistory>() {
        @Override
        public TransactionHistory createFromParcel(Parcel in) {
            return new TransactionHistory(in);
        }

        @Override
        public TransactionHistory[] newArray(int size) {
            return new TransactionHistory[size];
        }
    };

    public String getOriginatingMobileNumber() {
        return ProfileInfoCacheManager.getMobileNumber();
    }

    public double getAmount() {
        return amount;
    }

    public double getFee() {
        return fee;
    }

    public String getReceiver() {
        if (otherParty.getName() == null) {
            return otherParty.getNumber();
        } else {
            return otherParty.getName();
        }
    }

    public double getNetAmount() {
        return netAmount;
    }

    public String getNetAmountFormatted() {
        if (statusCode != Constants.TRANSACTION_STATUS_CANCELLED && statusCode != Constants.TRANSACTION_STATUS_FAILED && statusCode != Constants.TRANSACTION_STATUS_REJECTED) {
            if (type.equals(Constants.TRANSACTION_TYPE_CREDIT))
                return "+" + netAmount;
            else if (type.equals(Constants.TRANSACTION_TYPE_DEBIT))
                return "-" + netAmount;
            return "" + netAmount;
        }
        return "" + netAmount;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public int getServiceID() {
        return serviceId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getPurpose() {
        return purpose;
    }

    public long getTime() {
        return insertTime;
    }

    public String getTransactionID() {
        return transactionId;
    }

    public TransactionHistoryAdditionalInfo getAdditionalInfo() {
        return otherParty;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        if (shortDesc != null)
            return shortDesc;
        return "No Information Available";
    }

    public String getAccountId() {
        return accountId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getMessage() {
        return message;
    }

    public String getStatusInWord() {
        return statusInWord;
    }

    public String getType() {
        return type;
    }

    public Double getAccountBalance() {
        return accountBalance;
    }

    public TransactionHistoryAdditionalInfo getOtherParty() {
        return otherParty;
    }

    public TransactionMetaData getMetaData() {
        return metaData;
    }

    public String[] getActions() {
        return actions;
    }

    public String getStatus() {
        return statusInWord;
    }

    public String getOutletName() {
        return outletName;
    }

    public long getOutletId() {
        return outletId;
    }

    @Override
    public String toString() {
        return "TransactionHistory{" +
                "insertTime=" + insertTime +
                ", outletName=" + outletName + '\'' +
                ", outletId=" + outletId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", serviceId=" + serviceId +
                ", transactionId='" + transactionId + '\'' +
                ", message='" + message + '\'' +
                ", amount=" + amount +
                ", fee=" + fee +
                ", netAmount=" + netAmount +
                ", purpose='" + purpose + '\'' +
                ", description='" + description + '\'' +
                ", statusCode=" + statusCode +
                ", statusInWord='" + statusInWord + '\'' +
                ", type='" + type + '\'' +
                ", accountBalance=" + accountBalance +
                ", availableBalance=" + availableBalance +
                ", otherParty=" + otherParty +
                ", actions=" + Arrays.toString(actions) +
                ", metaData=" + metaData +
                '}';
    }
}
