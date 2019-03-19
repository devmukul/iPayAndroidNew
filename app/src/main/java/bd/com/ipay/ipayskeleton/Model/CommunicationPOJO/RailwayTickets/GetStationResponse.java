
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GetStationResponse implements Serializable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("stationList")
    @Expose
    private List<String> stationList = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getStationList() {
        return stationList;
    }

    public void setStationList(List<String> stationList) {
        this.stationList = stationList;
    }

}
