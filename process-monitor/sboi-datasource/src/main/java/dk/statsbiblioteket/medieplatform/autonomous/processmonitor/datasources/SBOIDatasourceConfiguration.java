package dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources;

public class SBOIDatasourceConfiguration {


    private String summaLocation;
    private String domsLocation;
    private String domsUser;
    private String domsPassword;
    private int sboiPageSize = 100;

    public String getSummaLocation() {
        return summaLocation;
    }

    public void setSummaLocation(String summaLocation) {
        this.summaLocation = summaLocation;
    }

    public String getDomsLocation() {
        return domsLocation;
    }

    public String getDomsUser() {
        return domsUser;
    }

    public String getDomsPassword() {
        return domsPassword;
    }

    public void setDomsLocation(String domsLocation) {
        this.domsLocation = domsLocation;
    }

    public void setDomsUser(String domsUser) {
        this.domsUser = domsUser;
    }

    public void setDomsPassword(String domsPassword) {
        this.domsPassword = domsPassword;
    }

    public int getSboiPageSize() {
        return sboiPageSize;
    }

    public void setSboiPageSize(int sboiPageSize) {
        this.sboiPageSize = sboiPageSize;
    }
}
