package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;


public class DpdcBillPayRequest {
    private String accountNumber;
    private String billMonths;
    private String billYears;
    private String chequeRemarks;
    private String pin;
    private String otp;
    private String locationCode;

    public DpdcBillPayRequest(String accountNumber, String billMonths, String billYears, String chequeRemarks, String pin, String locationCode) {
        this.accountNumber = accountNumber;
        this.billMonths = billMonths;
        this.billYears = billYears;
        this.chequeRemarks = chequeRemarks;
        this.pin = pin;
        this.locationCode = locationCode;
    }

    public DpdcBillPayRequest(String accountNumber, String billMonths, String billYears, String chequeRemarks, String pin, String otp, String locationCode) {
        this.accountNumber = accountNumber;
        this.billMonths = billMonths;
        this.billYears = billYears;
        this.chequeRemarks = chequeRemarks;
        this.pin = pin;
        this.otp = otp;
        this.locationCode = locationCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBillMonths() {
        return billMonths;
    }

    public void setBillMonths(String billMonths) {
        this.billMonths = billMonths;
    }

    public String getBillYears() {
        return billYears;
    }

    public void setBillYears(String billYears) {
        this.billYears = billYears;
    }

    public String getChequeRemarks() {
        return chequeRemarks;
    }

    public void setChequeRemarks(String chequeRemarks) {
        this.chequeRemarks = chequeRemarks;
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

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }
}
