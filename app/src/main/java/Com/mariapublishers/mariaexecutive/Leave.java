package Com.mariapublishers.mariaexecutive;

public class Leave {
    String leaveId;
    String reason;
    String fromDate;
    String toDate;
    String status;

    String userId;
    String userName;

    public Leave(String leaveId, String reason, String leaveDate, String toDate, String status) {
        this.leaveId = leaveId;
        this.reason = reason;
        this.fromDate = leaveDate;
        this.toDate = toDate;
        this.status = status;
    }

    public Leave(String leaveId, String reason, String leaveDate, String toDate, String userId, String userName, String status) {
        this.leaveId = leaveId;
        this.reason = reason;
        this.fromDate = leaveDate;
        this.toDate = toDate;
        this.userId = userId;
        this.userName = userName;
        this.status= status;
    }

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
