package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

import java.util.List;

public class GetScheduledPaymentInfoResponse {
    private List<GroupedScheduledPaymentInfo>groupedScheduledPaymentList;

    public List<GroupedScheduledPaymentInfo> getGroupedScheduledPaymentList() {
        return groupedScheduledPaymentList;
    }

    public void setGroupedScheduledPaymentList(List<GroupedScheduledPaymentInfo> groupedScheduledPaymentList) {
        this.groupedScheduledPaymentList = groupedScheduledPaymentList;
    }
}
