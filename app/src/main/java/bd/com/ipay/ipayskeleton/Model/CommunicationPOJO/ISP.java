package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

public class ISP {

    private String ispName;
    private String ispCode;
    private int ispIconDrawable;

    public ISP(String ispName, String ispCode, int ispIconDrawable) {
        this.ispName = ispName;
        this.ispCode = ispCode;
        this.ispIconDrawable = ispIconDrawable;
    }

    public String getIspName() {
        return ispName;
    }

    public void setIspName(String ispName) {
        this.ispName = ispName;
    }

    public String getIspCode() {
        return ispCode;
    }

    public void setIspCode(String ispCode) {
        this.ispCode = ispCode;
    }

    public int getIspIconDrawable() {
        return ispIconDrawable;
    }

    public void setIspIconDrawable(int ispIconDrawable) {
        this.ispIconDrawable = ispIconDrawable;
    }
}
