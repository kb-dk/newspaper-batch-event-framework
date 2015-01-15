package dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources;

public class SBOIDatasourceConfiguration {


    private String summaLocation;
    private String domsLocation;
    private String domsUser;
    private String domsPassword;
    private String domsPidGenLocation;
    private int sboiPageSize = 100;
    private String domsRetries;
    private String domsDelayBetweenRetries;

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

    public String getDomsPidGenLocation() {
        return domsPidGenLocation;
    }

    public void setDomsPidGenLocation(String domsPidGenLocation) {
        this.domsPidGenLocation = domsPidGenLocation;
    }

    public String getDomsRetries() {
        return domsRetries;
    }

    public void setDomsRetries(String domsRetries) {
        this.domsRetries = domsRetries;
    }

    public String getDomsDelayBetweenRetries() {
        return domsDelayBetweenRetries;
    }

    public void setDomsDelayBetweenRetries(String domsDelayBetweenRetries) {
        this.domsDelayBetweenRetries = domsDelayBetweenRetries;
    }
}
