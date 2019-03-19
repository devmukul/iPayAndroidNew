
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.V2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class IPayHereResponse implements Serializable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("nearbyBusinessResponseList")
    @Expose
    private List<NearbyBusinessResponseList> nearbyBusinessResponseList = null;
    private final static long serialVersionUID = -8468909633342281059L;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NearbyBusinessResponseList> getNearbyBusinessResponseList() {
        return nearbyBusinessResponseList;
    }

    public void setNearbyBusinessResponseList(List<NearbyBusinessResponseList> nearbyBusinessResponseList) {
        this.nearbyBusinessResponseList = nearbyBusinessResponseList;
    }

}
