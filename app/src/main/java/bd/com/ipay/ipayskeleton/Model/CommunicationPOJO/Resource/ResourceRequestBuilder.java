package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource;

import android.net.Uri;

import bd.com.ipay.ipayskeleton.Utilities.Constants;

public abstract class ResourceRequestBuilder {
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_FILTER = "filter";

    private String generatedUri;
    private final long filter;

    ResourceRequestBuilder() {
        filter = -1;
        generateUri();
    }

    ResourceRequestBuilder(long filter) {
        this.filter = filter;
        generateUri();
    }

    private void generateUri() {

        Uri uri;
        if (filter == -1) uri = Uri.parse(Constants.BASE_URL_MM + Constants.URL_RESOURCE)
                .buildUpon()
                .appendQueryParameter(PARAM_TYPE, getResourceType())
                .build();
        else uri = Uri.parse(Constants.BASE_URL_MM + Constants.URL_RESOURCE)
                .buildUpon()
                .appendQueryParameter(PARAM_TYPE, getResourceType())
                .appendQueryParameter(PARAM_FILTER, filter + "")
                .build();

        setGeneratedUri(uri.toString());
    }

    protected abstract String getResourceType();

    public String getGeneratedUri() {
        return generatedUri;
    }

    private void setGeneratedUri(String generatedUri) {
        this.generatedUri = generatedUri;
    }
}
