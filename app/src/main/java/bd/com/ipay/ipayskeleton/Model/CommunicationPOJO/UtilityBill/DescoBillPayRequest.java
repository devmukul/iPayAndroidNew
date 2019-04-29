package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;


public class DescoBillPayRequest {
    private String billNumber;
    private String pin;
    private String otp;
    private MetaData metaData;

    public DescoBillPayRequest(String billNumber, String pin) {
        this.billNumber = billNumber;
        this.pin = pin;
    }

    public DescoBillPayRequest(String billNumber, String pin, String otp) {
        this.billNumber = billNumber;
        this.pin = pin;
        this.otp = otp;
    }

    public DescoBillPayRequest(String billNumber, String pin, MetaData metaData) {
        this.billNumber = billNumber;
        this.pin = pin;
        this.metaData = metaData;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public String getPin() {
        return pin;
    }

    public String getOtp() {
        return otp;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
