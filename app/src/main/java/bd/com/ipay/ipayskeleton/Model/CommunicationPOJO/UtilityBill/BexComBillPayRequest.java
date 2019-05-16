package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;

public class BexComBillPayRequest {
    private final String subscriberId;
    private final double amount;
    private final String pin;
    private String otp;

    public BexComBillPayRequest(String subscriberId, String amount, String pin) {
        this.subscriberId = subscriberId;
        this.amount = Double.parseDouble(amount);
        this.pin = pin;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

}
