package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

public class AddMoneyByBracBank {
    private String transactionId;

    private String otp;

    public AddMoneyByBracBank() {
    }

    public AddMoneyByBracBank(String transactionId, String otp) {
        this.transactionId = transactionId;
        this.otp = otp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOtp() {
        return otp;
    }
}
