package bd.com.ipay.ipayskeleton.Model.Friend;

public class FriendNode implements Comparable<FriendNode> {
    private String phoneNumber;
    private FriendInfo info;

    public FriendNode() {

    }

    public FriendNode(String phoneNumber, FriendInfo info) {
        this.phoneNumber = phoneNumber;
        this.info = info;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public FriendInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "FriendNode{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", info=" + info +
                '}';
    }

    @Override
    public int compareTo(FriendNode another) {
        return phoneNumber.compareTo(another.phoneNumber);
    }
}