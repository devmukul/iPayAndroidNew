
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InstallmentInfoList implements Serializable
{

    @SerializedName("amount")
    @Expose
    private Long amount;
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("status")
    @Expose
    private Long status;
    @SerializedName("triggerDate")
    @Expose
    private Long triggerDate;
    private final static long serialVersionUID = -135832299628879036L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public InstallmentInfoList() {
    }

    /**
     * 
     * @param id
     * @param amount
     * @param triggerDate
     * @param status
     */
    public InstallmentInfoList(Long amount, Long id, Long status, Long triggerDate) {
        super();
        this.amount = amount;
        this.id = id;
        this.status = status;
        this.triggerDate = triggerDate;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(Long triggerDate) {
        this.triggerDate = triggerDate;
    }

}
