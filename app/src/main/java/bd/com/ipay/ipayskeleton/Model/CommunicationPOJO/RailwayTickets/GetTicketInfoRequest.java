
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetTicketInfoRequest implements Serializable
{

    @SerializedName("age")
    @Expose
    private int age;
    @SerializedName("className")
    @Expose
    private String className;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("journeyDate")
    @Expose
    private int journeyDate;
    @SerializedName("numberOfAdults")
    @Expose
    private int numberOfAdults;
    @SerializedName("numberOfChildren")
    @Expose
    private int numberOfChildren;
    @SerializedName("stationFrom")
    @Expose
    private String stationFrom;
    @SerializedName("stationTo")
    @Expose
    private String stationTo;
    @SerializedName("trainNumber")
    @Expose
    private int trainNumber;

    public GetTicketInfoRequest(int age, String className, String gender, int journeyDate, int numberOfAdults, int numberOfChildren, String stationFrom, String stationTo, int trainNumber) {
        this.age = age;
        this.className = className;
        this.gender = gender;
        this.journeyDate = journeyDate;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
        this.trainNumber = trainNumber;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(int journeyDate) {
        this.journeyDate = journeyDate;
    }

    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(int numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public String getStationFrom() {
        return stationFrom;
    }

    public void setStationFrom(String stationFrom) {
        this.stationFrom = stationFrom;
    }

    public String getStationTo() {
        return stationTo;
    }

    public void setStationTo(String stationTo) {
        this.stationTo = stationTo;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
    }

}
