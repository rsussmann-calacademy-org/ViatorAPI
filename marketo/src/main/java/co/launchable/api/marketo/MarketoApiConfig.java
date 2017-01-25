package co.launchable.api.marketo;

import com.marketo.mktows.MktowsPort;

/**
 * Created by Michael on 5/13/2015.
 */
public class MarketoApiConfig {
    private String marketoServiceQnameUrl;
    private String marketoServiceQnameName;
    private String marketoUserId;
    private String marketoSecretKey;
    private String marketoEndpoint;
    private MktowsPort marketoWebServicePort;

    public MktowsPort getMarketoWebServicePort() {
        return marketoWebServicePort;
    }

    public void setMarketoWebServicePort(MktowsPort marketoWebServicePort) {
        this.marketoWebServicePort = marketoWebServicePort;
    }

    public String getMarketoEndpoint() {
        return marketoEndpoint;
    }

    public void setMarketoEndpoint(String marketoEndpoint) {
        this.marketoEndpoint = marketoEndpoint;
    }

    public String getMarketoServiceQnameUrl() {
        return marketoServiceQnameUrl;
    }

    public void setMarketoServiceQnameUrl(String marketoServiceQnameUrl) {
        this.marketoServiceQnameUrl = marketoServiceQnameUrl;
    }

    public String getMarketoServiceQnameName() {
        return marketoServiceQnameName;
    }

    public void setMarketoServiceQnameName(String marketoServiceQnameName) {
        this.marketoServiceQnameName = marketoServiceQnameName;
    }

    public String getMarketoUserId() {
        return marketoUserId;
    }

    public void setMarketoUserId(String marketoUserId) {
        this.marketoUserId = marketoUserId;
    }

    public String getMarketoSecretKey() {
        return marketoSecretKey;
    }

    public void setMarketoSecretKey(String marketoSecretKey) {
        this.marketoSecretKey = marketoSecretKey;
    }
}
