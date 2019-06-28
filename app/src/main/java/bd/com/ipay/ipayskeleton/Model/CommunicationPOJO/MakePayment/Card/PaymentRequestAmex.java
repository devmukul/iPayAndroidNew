package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card;


import com.google.gson.annotations.SerializedName;

public class PaymentRequestAmex {

    private final String mobileNumber;
    private final double amount;
    private final String description;
    private final String ref;
    private final Long outletId;
    @SerializedName("lat")
    private Double latitude;
    @SerializedName("lng")
    private Double longitude;
    private String cardBrand;

    private String cardId;

    private String cvv;

    public PaymentRequestAmex(String mobileNumber, String amount, String description, String ref, Long outletId, double latitude, double longitude, String cardBrand) {
        this.mobileNumber = mobileNumber;
        this.amount = Double.parseDouble(amount);
        this.description = description;
        this.ref = ref;
        this.outletId = outletId;
        if (latitude != 0.0 && longitude != 0.0) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        this.cardBrand = cardBrand;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
}
