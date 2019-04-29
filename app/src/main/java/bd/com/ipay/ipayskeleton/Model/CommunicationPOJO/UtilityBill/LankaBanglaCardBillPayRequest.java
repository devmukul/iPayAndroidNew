package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;

public class LankaBanglaCardBillPayRequest {
    private String cardNumber;
    private String amount;
    private String amountType;
    private String pin;
    private String otp;
    bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData metaData;

    public LankaBanglaCardBillPayRequest(String cardNumber, String amount, String amountType, String pin) {
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.amountType = amountType;
        this.pin = pin;
    }

    public LankaBanglaCardBillPayRequest(String cardNumber, String amount, String amountType, String pin, MetaData metaData) {
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.amountType = amountType;
        this.pin = pin;
        this.otp = otp;
        this.metaData = metaData;
    }

    public String getCardNumber() {

        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LankaBanglaCardBillPayRequest(String cardNumber, String amount, String amountType, String pin, String otp) {

        this.cardNumber = cardNumber;
        this.amount = amount;
        this.amountType = amountType;
        this.pin = pin;
        this.otp = otp;
    }
}
