package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment;


public class GetOrderDetails {

    private String message;
    private String merchantName;
    private String merchantLogoUrl;
    private String description;
    private double amount;

    public String getMessage() {
        return message;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantLogoUrl() {
        return merchantLogoUrl;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }
}
