package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email;

import java.util.List;

public class GetEmailResponse {
    private String message;
    private List<Email> emailAdressList;

    public String getMessage() {
        return message;
    }

    public List<Email> getEmailAdressList() {
        return emailAdressList;
    }

    public String getVerifiedEmail() {
        for (Email email : emailAdressList) {
            if (email.isPrimary())
                return email.getEmailAddress();
        }

        return null;
    }
}
