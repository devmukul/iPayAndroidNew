
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetTicketInfoResponse implements Serializable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("ticketId")
    @Expose
    private String ticketId;
    @SerializedName("messageId")
    @Expose
    private String messageId;
    @SerializedName("journeyDate")
    @Expose
    private String journeyDate;
    @SerializedName("trainNumber")
    @Expose
    private int trainNumber;
    @SerializedName("stationFrom")
    @Expose
    private String stationFrom;
    @SerializedName("stationTo")
    @Expose
    private String stationTo;
    @SerializedName("className")
    @Expose
    private String className;
    @SerializedName("fare")
    @Expose
    private double fare;
    @SerializedName("vat")
    @Expose
    private double vat;
    @SerializedName("totalFare")
    @Expose
    private double totalFare;

    public GetTicketInfoResponse() {
    }

    public GetTicketInfoResponse(String message, String ticketId, String messageId, String journeyDate, int trainNumber, String stationFrom, String stationTo, String className, double fare, double vat, double totalFare) {
        this.message = message;
        this.ticketId = ticketId;
        this.messageId = messageId;
        this.journeyDate = journeyDate;
        this.trainNumber = trainNumber;
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
        this.className = className;
        this.fare = fare;
        this.vat = vat;
        this.totalFare = totalFare;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    @Override
    public String toString() {
        return "GetTicketInfoResponse{" +
                "message='" + message + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", journeyDate='" + journeyDate + '\'' +
                ", trainNumber=" + trainNumber +
                ", stationFrom='" + stationFrom + '\'' +
                ", stationTo='" + stationTo + '\'' +
                ", className='" + className + '\'' +
                ", fare=" + fare +
                ", vat=" + vat +
                ", totalFare=" + totalFare +
                '}';
    }
}
