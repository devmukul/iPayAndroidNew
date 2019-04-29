package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;


public class LankaBanglaDpsBillPayRequest {

    private String dpsAccountNumber;
    private String pin;
    private String otp;
    private MetaData metaData;

    public LankaBanglaDpsBillPayRequest(String dpsAccountNumber, String pin) {
        this.dpsAccountNumber = dpsAccountNumber;
        this.pin = pin;
    }

    public LankaBanglaDpsBillPayRequest(String dpsAccountNumber, String pin, String otp) {

        this.dpsAccountNumber = dpsAccountNumber;
        this.pin = pin;
        this.otp = otp;
    }

    public LankaBanglaDpsBillPayRequest(String dpsAccountNumber, String pin, MetaData metaData) {
        this.dpsAccountNumber = dpsAccountNumber;
        this.pin = pin;
        this.metaData = metaData;
    }
}
