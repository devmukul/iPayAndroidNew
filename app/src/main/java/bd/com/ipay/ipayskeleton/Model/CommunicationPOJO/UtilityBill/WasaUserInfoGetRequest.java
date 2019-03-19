package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;


public class WasaUserInfoGetRequest {
    private String accountNumber;
    private String billNumber;

    public WasaUserInfoGetRequest(String accountNumber, String billNumber) {
        this.accountNumber = accountNumber;
        this.billNumber = billNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }
}
