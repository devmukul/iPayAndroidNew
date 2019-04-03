package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

public class WithdrawMoneyRequestV3 {

    private final long userBankId;
    private final double amount;
    private final String description;

    public WithdrawMoneyRequestV3(long userBankId, double amount, String description) {
        this.userBankId = userBankId;
        this.amount = amount;
        this.description = description;
    }
}
