package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;


public class DPDCUserInfoGetRequest {
    private String accountNumber;
    private String locationCode;
    private String billMonths;
    private String billYears;

    public DPDCUserInfoGetRequest(String accountNumber, String locationCode, String billMonths, String billYears) {
        this.accountNumber = accountNumber;
        this.locationCode = locationCode;
        this.billMonths = billMonths;
        this.billYears = billYears;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
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
}
