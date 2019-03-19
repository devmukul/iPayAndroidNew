
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PurchaseTicketRequest implements Serializable
{

    @SerializedName("className")
    @Expose
    private String className;
    @SerializedName("fare")
    @Expose
    private double fare;
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
    @SerializedName("otp")
    @Expose
    private String otp;
    @SerializedName("pin")
    @Expose
    private String pin;
    @SerializedName("stationFrom")
    @Expose
    private String stationFrom;
    @SerializedName("stationTo")
    @Expose
    private String stationTo;
    @SerializedName("ticketId")
    @Expose
    private String ticketId;
    @SerializedName("totalFare")
    @Expose
    private double totalFare;
    @SerializedName("trainNumber")
    @Expose
    private int trainNumber;
    @SerializedName("vat")
    @Expose
    private double vat;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PurchaseTicketRequest() {
    }

    /**
     * 
     * @param journeyDate
     * @param vat
     * @param fare
     * @param stationFrom
     * @param numberOfAdults
     * @param ticketId
     * @param totalFare
     * @param stationTo
     * @param pin
     * @param numberOfChildren
     * @param gender
     * @param className
     * @param trainNumber
     * @param otp
     */
    public PurchaseTicketRequest(String className, double fare, String gender, int journeyDate, int numberOfAdults, int numberOfChildren, String otp, String pin, String stationFrom, String stationTo, String ticketId, double totalFare, int trainNumber, double vat) {
        super();
        this.className = className;
        this.fare = fare;
        this.gender = gender;
        this.journeyDate = journeyDate;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
        this.otp = otp;
        this.pin = pin;
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
        this.ticketId = ticketId;
        this.totalFare = totalFare;
        this.trainNumber = trainNumber;
        this.vat = vat;
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

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
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

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

}
