package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment;

import java.math.BigDecimal;

public class SendNewPaymentRequest {

    private final BigDecimal amount;
    private final String clientMobileNumber;
    private final String description;
    private final Integer requestId;
    private final BigDecimal vat;

    public SendNewPaymentRequest(BigDecimal amount, String clientMobileNumber, String description, Integer requestId, BigDecimal vat) {
        this.amount = amount;
        this.clientMobileNumber = clientMobileNumber;
        this.description = description;
        this.requestId = requestId;
        this.vat = vat;
    }
}