package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

public class AddMoneyByCreditOrDebitCardRequest {

    private final double amount;
    private final String cardBrand;
    private final String note;
    private final String pin;

    public AddMoneyByCreditOrDebitCardRequest(double amount, String cardBrand, String note, String pin) {
        this.amount = amount;
        this.cardBrand = cardBrand;
        this.note = note;
        this.pin = pin;
    }
}
