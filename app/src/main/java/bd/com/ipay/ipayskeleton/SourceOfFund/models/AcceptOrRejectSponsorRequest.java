package bd.com.ipay.ipayskeleton.SourceOfFund.models;


public class AcceptOrRejectSponsorRequest {

    private String status;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AcceptOrRejectSponsorRequest(String status) {
        this.status = status;
    }
}
