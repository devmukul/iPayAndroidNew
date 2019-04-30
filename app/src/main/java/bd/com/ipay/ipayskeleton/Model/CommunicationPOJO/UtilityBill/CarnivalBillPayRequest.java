package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;


public class CarnivalBillPayRequest {
    private String userId;
    private String amount;
    private String pin;
    private String otp;
    private MetaData metaData;

    public CarnivalBillPayRequest(String userId, String amount, String pin) {
        this.userId = userId;
        this.amount = amount;
        this.pin = pin;
    }

    public CarnivalBillPayRequest(String userId, String amount, String pin, String otp) {

        this.userId = userId;
        this.amount = amount;
        this.pin = pin;
        this.otp = otp;
    }


    public CarnivalBillPayRequest(String userId, String amount, String pin, MetaData metaData) {
        this.userId = userId;
        this.amount = amount;
        this.pin = pin;
        this.metaData = metaData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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
}
