
package bd.com.ipay.ipayskeleton.Model.Rating;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Feedback implements Serializable
{

    @SerializedName("meta")
    @Expose
    private Meta meta;
    @SerializedName("rating")
    @Expose
    private int rating;
    @SerializedName("remark")
    @Expose
    private String remark;
    @SerializedName("serviceId")
    @Expose
    private int serviceId;
    @SerializedName("suggestionIds")
    @Expose
    private List<Long> suggestionIds = null;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public List<Long> getSuggestionIds() {
        return suggestionIds;
    }

    public void setSuggestionIds(List<Long> suggestionIds) {
        this.suggestionIds = suggestionIds;
    }

}
