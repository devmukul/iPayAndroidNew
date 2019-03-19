package bd.com.ipay.ipayskeleton.SourceOfFund.models;


import java.util.ArrayList;

public class GetBeneficiaryListResponse {

    private ArrayList<Beneficiary> beneficiary;
    private String message;

    public ArrayList<Beneficiary> getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(ArrayList<Beneficiary> beneficiary) {
        this.beneficiary = beneficiary;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
