package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource;

import android.content.Context;
import android.content.res.Resources;

public class Bank implements Resource {
    private int id;
    private String name;
    private String bankCode;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStringId() {
        return null;
    }

    public String getBankCode() {
        return bankCode;
    }

    public int getBankIcon(Context context) {
        Resources resources = context.getResources();
        int resourceId;
        if (bankCode != null) {
            try {
                resourceId = resources.getIdentifier("ic_bank" + getBankCode(), "drawable",
                        context.getPackageName());
            }catch (Exception e){
                e.printStackTrace();
                resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                        context.getPackageName());
            }

        }
        else
            resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                    context.getPackageName());
        return resourceId;
        //return resources.getDrawable(resourceId);
    }

    @Override
    public String toString() {
        return "Bank{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bankCode='" + bankCode + '\'' +
                '}';
    }
}
