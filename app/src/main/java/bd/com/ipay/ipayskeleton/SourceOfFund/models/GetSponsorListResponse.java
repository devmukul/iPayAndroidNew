package bd.com.ipay.ipayskeleton.SourceOfFund.models;

import java.util.ArrayList;


public class GetSponsorListResponse {
    private ArrayList<Sponsor> sponsor;
    private String message;

    public ArrayList<Sponsor> getSponsor() {
        return sponsor;
    }

    public void setSponsor(ArrayList<Sponsor> sponsor) {
        this.sponsor = sponsor;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

