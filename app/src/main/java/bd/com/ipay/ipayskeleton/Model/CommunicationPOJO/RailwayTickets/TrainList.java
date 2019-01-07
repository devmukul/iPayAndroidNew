
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrainList implements Serializable
{

    @SerializedName("trainNumber")
    @Expose
    Long trainNumber;
    @SerializedName("trainName")
    @Expose
    String trainName;
    @SerializedName("departureTime")
    @Expose
    String departureTime;
    @SerializedName("classList")
    @Expose
    List<String> classList = null;


    public TrainList() {
    }

    public TrainList(Long trainNumber, String trainName, String departureTime, List<String> classList) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.departureTime = departureTime;
        this.classList = classList;
    }

    public Long getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(Long trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public List<String> getClassList() {
        return classList;
    }

    public void setClassList(List<String> classList) {
        this.classList = classList;
    }

    @Override
    public String toString() {
        return "TrainList{" +
                "trainNumber=" + trainNumber +
                ", trainName='" + trainName + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", classList=" + classList +
                '}';
    }
}
