package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RefreshToken;

public class FcmLogoutRequest {
    private String firebaseToken;


    public FcmLogoutRequest(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }
}
