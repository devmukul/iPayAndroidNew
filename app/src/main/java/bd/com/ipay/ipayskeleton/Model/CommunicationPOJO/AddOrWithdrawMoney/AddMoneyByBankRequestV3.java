package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

public class AddMoneyByBankRequestV3 {
    private long userBankId;
    private double amount;
    private String description;

    public AddMoneyByBankRequestV3(long userBankId, double amount, String description) {
        this.userBankId = userBankId;
        this.amount = amount;
        this.description = description;
    }

    public long getUserBankId() {
        return userBankId;
    }

    public void setUserBankId(long userBankId) {
        this.userBankId = userBankId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
