package bd.com.ipay.ipayskeleton.Model.MMModule.AddOrWithdrawMoney;

public class AddMoneyRequest {

    private final long bankAccountId;
    private final double amount;
    private final String description;
    private final String pin;

    public AddMoneyRequest(long bankAccountId, double amount, String description, String pin) {
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.description = description;
        this.pin = pin;
    }
}
