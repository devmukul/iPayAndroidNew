package bd.com.ipay.ipayskeleton.SourceOfFund.models;


public class AddSponsorRequest {

    private String mobileNumber;
    private String relation;
    private long  monthlyCreditLimit;

    public AddSponsorRequest(String mobileNumber, String relation) {
        this.mobileNumber = mobileNumber;
        this.relation = relation;
    }

    public AddSponsorRequest(String mobileNumber, String relation, long monthlyCreditLimit) {
        this.mobileNumber = mobileNumber;
        this.relation = relation;
        this.monthlyCreditLimit = monthlyCreditLimit;
    }

    public String getRelation() {
        return relation;
    }

    public long getMonthlyCreditLimit() {
        return monthlyCreditLimit;
    }

    public void setMonthlyCreditLimit(long monthlyCreditLimit) {
        this.monthlyCreditLimit = monthlyCreditLimit;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getMobileNumber() {

        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
