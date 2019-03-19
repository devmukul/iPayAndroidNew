package bd.com.ipay.ipayskeleton.SourceOfFund.models;



public class RemoveSponsorOrBeneficiaryRequest {
    public RemoveSponsorOrBeneficiaryRequest(String pin) {
        this.pin = pin;
    }

    private String pin;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
