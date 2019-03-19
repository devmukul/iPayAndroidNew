
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class GetTrainListResponse implements Serializable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("trainList")
    @Expose
    private ArrayList<TrainList> trainList = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<TrainList> getTrainList() {
        return trainList;
    }

    public void setTrainList(ArrayList<TrainList> trainList) {
        this.trainList = trainList;
    }

}
