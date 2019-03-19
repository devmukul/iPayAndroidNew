
package bd.com.ipay.ipayskeleton.Model.Rating;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RatingSubmitRequestBuilder implements Serializable
{

    @SerializedName("feedbacks")
    @Expose
    private List<Feedback> feedbacks = null;

    public RatingSubmitRequestBuilder(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

}
