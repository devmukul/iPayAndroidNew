package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;

public class DescoCustomerInfoResponse {
    private String accountNumber;
    private String transactionId;
    private String billNumber;
    private String zoneCode;
    private String message;
    private String vatAmount;
    private String billAmount;
    private String lpcAmount;
    private String stampAmount;
    private String totalAmount;
    private String dueDate;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getVatAmount() {
        return vatAmount;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public String getMessage() {
        return message;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setVatAmount(String vatAmount) {
        this.vatAmount = vatAmount;
    }

    public String getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(String billAmount) {
        this.billAmount = billAmount;
    }

    public String getLpcAmount() {
        return lpcAmount;
    }

    public void setLpcAmount(String lpcAmount) {
        this.lpcAmount = lpcAmount;
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

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
