package dk.statsbiblioteket.newspaper;

public class SBOIDatasourceConfiguration {


    private String summaLocation;
    private String domsUrl;
    private String domsUser;
    private String domsPass;
    private String urlToPidGen;

    public String getSummaLocation() {
        return summaLocation;
    }

    public void setSummaLocation(String summaLocation) {
        this.summaLocation = summaLocation;
    }

    public String getDomsUrl() {
        return domsUrl;
    }

    public String getDomsUser() {
        return domsUser;
    }

    public String getDomsPass() {
        return domsPass;
    }

    public String getUrlToPidGen() {
        return urlToPidGen;
    }

    public void setDomsUrl(String domsUrl) {
        this.domsUrl = domsUrl;
    }

    public void setDomsUser(String domsUser) {
        this.domsUser = domsUser;
    }

    public void setDomsPass(String domsPass) {
        this.domsPass = domsPass;
    }

    public void setUrlToPidGen(String urlToPidGen) {
        this.urlToPidGen = urlToPidGen;
    }
}
