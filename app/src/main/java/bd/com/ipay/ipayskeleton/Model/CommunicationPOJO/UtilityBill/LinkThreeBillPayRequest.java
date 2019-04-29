package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;

public class LinkThreeBillPayRequest {
    private final String subscriberId;
    private final double amount;
    private final String pin;
    private String otp;
    private MetaData metaData;

    public LinkThreeBillPayRequest(String subscriberId, String amount, String pin) {
        this.subscriberId = subscriberId;
        this.amount = Double.parseDouble(amount);
        this.pin = pin;
    }

    public LinkThreeBillPayRequest(String subscriberId, String amount, String pin, MetaData metaData) {
        this.subscriberId = subscriberId;
        this.amount = Double.parseDouble(amount);
        this.pin = pin;
        this.metaData = metaData;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

}
