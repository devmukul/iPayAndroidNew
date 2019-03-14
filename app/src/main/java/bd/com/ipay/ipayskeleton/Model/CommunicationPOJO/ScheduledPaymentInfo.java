package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

public class ScheduledPaymentInfo {
    private String center;
    private long createdAt;
    private long updatedAt;
    private String description;
    private long id;
    private long installmentAmount;
    private long installmentNumber;
    private String product;
    private UserInfo receiverInfo;
    private UserInfo customerInfo;

    private String schedulerType;
    private long startDate;

    public UserInfo getReceiverInfo() {
        return receiverInfo;
    }

    public void setReceiverInfo(UserInfo receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(long installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public long getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(long installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }
}
