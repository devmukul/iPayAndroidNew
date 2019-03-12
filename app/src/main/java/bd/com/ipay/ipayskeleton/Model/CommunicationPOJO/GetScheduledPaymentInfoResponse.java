package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

import java.util.List;

public class GetScheduledPaymentInfoResponse {
    private List<ScheduledPaymentInfo>groupedScheduledPaymentList;

    public List<ScheduledPaymentInfo> getGroupedScheduledPaymentList() {
        return groupedScheduledPaymentList;
    }

    public void setGroupedScheduledPaymentList(List<ScheduledPaymentInfo> groupedScheduledPaymentList) {
        this.groupedScheduledPaymentList = groupedScheduledPaymentList;
    }
}
