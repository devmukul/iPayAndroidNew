package bd.com.ipay.ipayskeleton.Model.MMModule.TrustedDevice;

public class AddToTrustedDeviceRequest {

    private String deviceName;
    private String deviceId;

    public AddToTrustedDeviceRequest(String deviceName, String deviceId) {
        this.deviceName = deviceName;
        this.deviceId = deviceId;
    }
}
