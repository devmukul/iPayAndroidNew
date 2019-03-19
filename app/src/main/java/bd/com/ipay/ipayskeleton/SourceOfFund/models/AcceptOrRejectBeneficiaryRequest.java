package bd.com.ipay.ipayskeleton.SourceOfFund.models;


public class AcceptOrRejectBeneficiaryRequest {
    private long monthlyCreditLimit;
    private String pin;
    private String status;

    public AcceptOrRejectBeneficiaryRequest(String pin, String status) {
        this.pin = pin;
        this.status = status;
    }

    public AcceptOrRejectBeneficiaryRequest(long monthlyCreditLimit, String pin, String status) {
        this.monthlyCreditLimit = monthlyCreditLimit;
        this.pin = pin;
        this.status = status;
    }

    public long getMonthlyCreditLimit() {

        return monthlyCreditLimit;
    }

    public void setMonthlyCreditLimit(long monthlyCreditLimit) {
        this.monthlyCreditLimit = monthlyCreditLimit;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
