package bd.com.ipay.ipayskeleton.SourceOfFund.models;


public class AddBeneficiaryRequest {
    private String mobileNumber;
    private long monthlyCreditLimit;
    private String pin;
    private String relation;

    public AddBeneficiaryRequest(String mobileNumber, long monthlyCreditLimit, String pin, String relation) {
        this.mobileNumber = mobileNumber;
        this.monthlyCreditLimit = monthlyCreditLimit;
        this.pin = pin;
        this.relation = relation;
    }

    public AddBeneficiaryRequest(String mobileNumber, String pin, String relation) {
        this.mobileNumber = mobileNumber;
        this.pin = pin;
        this.relation = relation;
    }

    public String getMobileNumber() {

        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
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

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
