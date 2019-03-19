
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.NewsList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NewsList implements Serializable
{

    @SerializedName("bodyContent")
    @Expose
    private String bodyContent;
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("subTitle")
    @Expose
    private String subTitle;
    @SerializedName("templateId")
    @Expose
    private int templateId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("urlExtension")
    @Expose
    private String urlExtension;
    @SerializedName("urlPlaceholder")
    @Expose
    private String urlPlaceholder;

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrlExtension() {
        return urlExtension;
    }

    public void setUrlExtension(String urlExtension) {
        this.urlExtension = urlExtension;
    }

    public String getUrlPlaceholder() {
        return urlPlaceholder;
    }

    public void setUrlPlaceholder(String urlPlaceholder) {
        this.urlPlaceholder = urlPlaceholder;
    }

}
