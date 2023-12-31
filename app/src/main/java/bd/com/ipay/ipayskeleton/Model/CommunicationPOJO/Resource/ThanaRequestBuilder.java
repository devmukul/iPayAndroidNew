package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource;

public class ThanaRequestBuilder extends ResourceRequestBuilder {

    private static final String RESOURCE_TYPE_THANA = "thana";

    public ThanaRequestBuilder() {
        super();
    }

    public ThanaRequestBuilder(int filter) {
        super(filter);
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE_THANA;
    }
}
