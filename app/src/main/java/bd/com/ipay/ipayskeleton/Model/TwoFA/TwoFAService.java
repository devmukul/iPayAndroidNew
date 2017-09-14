package bd.com.ipay.ipayskeleton.Model.TwoFA;

/**
 * Created by sourav.saha on 9/6/17.
 */

public class TwoFAService {

    private String serviceId;
    private String serviceName;
    private boolean isEnabled;

    public TwoFAService() {
    }

    public TwoFAService(String serviceId, String serviceName, boolean isEnabled) {

        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.isEnabled = isEnabled;
    }

    public String getServiceId() {

        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public String toString() {
        return "TwoFAService{" +
                "serviceId='" + serviceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", isEnabled='" + isEnabled + '\'' +
                '}';
    }
}
