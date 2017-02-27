package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TopUp;

public class TopupRequest {

    private final long senderAccountID;
    private final String receiverMobileNumber;
    private final int mobileNumberType;
    private final int operatorCode;
    private final double amount;
    private final String countryCode;
    private final int senderAccountUserType;
    private final int senderAccountUserClass;
    private final String pin;

    public TopupRequest(long senderAccountID, String receiverMobileNumber,
                        int mobileNumberType, int operatorCode, double amount, String countryCode,
                        int senderAccountUserType, int senderAccountUserClass, String pin) {
        this.senderAccountID = senderAccountID;
        this.receiverMobileNumber = receiverMobileNumber;
        this.mobileNumberType = mobileNumberType;
        this.operatorCode = operatorCode;
        this.amount = amount;
        this.countryCode = countryCode;
        this.senderAccountUserClass = senderAccountUserClass;
        this.senderAccountUserType = senderAccountUserType;
        this.pin = pin;
    }
}