package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC;

import java.util.List;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GroupedScheduledPaymentInfo;

public class GetScheduledPaymentInfoResponse {
    private List<GroupedScheduledPaymentInfo>groupedScheduledPaymentList;

    public List<GroupedScheduledPaymentInfo> getGroupedScheduledPaymentList() {
        return groupedScheduledPaymentList;
    }

    public void setGroupedScheduledPaymentList(List<GroupedScheduledPaymentInfo> groupedScheduledPaymentList) {
        this.groupedScheduledPaymentList = groupedScheduledPaymentList;
    }
}
