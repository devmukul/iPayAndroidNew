
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.NewsList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GetNewsRoomFeedResponse implements Serializable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("newsList")
    @Expose
    private List<NewsList> newsList = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NewsList> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<NewsList> newsList) {
        this.newsList = newsList;
    }

}
