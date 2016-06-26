package bd.com.ipay.ipayskeleton.Model.MMModule.Business;

import java.util.List;

public class EmployeeDetails {
    private String designation;
    private int id;
    private String mobileNumber;
    private String name;
    private List<Privilege> privilegeList;
    private String profilePictureUrl;
    private String status;

    public String getDesignation() {
        return designation;
    }

    public int getId() {
        return id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getName() {
        return name;
    }

    public List<Privilege> getPrivilegeList() {
        return privilegeList;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getStatus() {
        return status;
    }
}
