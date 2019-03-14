package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

public class ISP {

    private String cardName;
    private String cardKey;
    private int cardIconDrawable;


    public ISP(String cardName, String cardKey, int cardIconDrawable) {
        this.cardName = cardName;
        this.cardKey = cardKey;
        this.cardIconDrawable = cardIconDrawable;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardKey() {
        return cardKey;
    }

    public void setCardKey(String cardKey) {
        this.cardKey = cardKey;
    }

    public int getCardIconDrawable() {
        return cardIconDrawable;
    }

    public void setCardIconDrawable(int cardIconDrawable) {
        this.cardIconDrawable = cardIconDrawable;
    }
}
