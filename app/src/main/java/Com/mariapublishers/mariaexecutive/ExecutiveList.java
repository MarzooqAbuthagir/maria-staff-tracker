package Com.mariapublishers.mariaexecutive;

public class ExecutiveList {
    String executiveId;
    String executiveName;

    public ExecutiveList(String executiveId, String executiveName) {
        this.executiveId = executiveId;
        this.executiveName = executiveName;
    }

    public String getExecutiveId() {
        return executiveId;
    }

    public void setExecutiveId(String executiveId) {
        this.executiveId = executiveId;
    }

    public String getExecutiveName() {
        return executiveName;
    }

    public void setExecutiveName(String executiveName) {
        this.executiveName = executiveName;
    }
}
