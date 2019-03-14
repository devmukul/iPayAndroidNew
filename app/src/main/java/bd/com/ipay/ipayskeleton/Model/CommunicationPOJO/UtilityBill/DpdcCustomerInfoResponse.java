package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;


public class DpdcCustomerInfoResponse {
    private String name;
    private String accountNumber;
    private String billAmount;
    private String vatAmount;
    private String stampAmount;
    private String totalAmount;
    private String dueDate;
    private String billNumber;
    private String message;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(String vatAmount) {
        this.vatAmount = vatAmount;
    }

    public String getStampAmount() {
        return stampAmount;
    }

    public void setStampAmount(String stampAmount) {
        this.stampAmount = stampAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(String billAmount) {
        this.billAmount = billAmount;
    }
}
