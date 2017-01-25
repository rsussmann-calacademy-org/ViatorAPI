package co.launchable.api.marketo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by mike on 4/5/16.
 */
public class MarketoRestApi {
    Logger log = Logger.getLogger(MarketoRestApi.class);

    private String clientId;
    private String clientSecret;
    private String clientToken;
    private String clientUser;
    private long clientTokenRetrievedAt;
    private long clientTokenExpiresIn;
    private String clientTokenType;

    private String restUrlEndpoint;
    private String restUrlIdentity;
    private String restUrlLeads;

    public long getClientTokenExpiresIn() {
        return clientTokenExpiresIn;
    }

    public void setClientTokenExpiresIn(long clientTokenExpiresIn) {
        this.clientTokenExpiresIn = clientTokenExpiresIn;
    }

    public long getClientTokenRetrievedAt() {
        return clientTokenRetrievedAt;
    }

    public void setClientTokenRetrievedAt(long clientTokenRetrievedAt) {
        this.clientTokenRetrievedAt = clientTokenRetrievedAt;
    }

    public String getClientTokenType() {
        return clientTokenType;
    }

    public void setClientTokenType(String clientTokenType) {
        this.clientTokenType = clientTokenType;
    }

    public String getRestUrlEndpoint() {
        return restUrlEndpoint;
    }

    public void setRestUrlEndpoint(String restUrlEndpoint) {
        this.restUrlEndpoint = restUrlEndpoint;
    }

    public String getRestUrlIdentity() {
        return restUrlIdentity;
    }

    public void setRestUrlIdentity(String restUrlIdentity) {
        this.restUrlIdentity = restUrlIdentity;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getClientUser() {
        return clientUser;
    }

    public void setClientUser(String clientUser) {
        this.clientUser = clientUser;
    }

    public void refreshAccessToken() {
        String accessTokenUrl = getRestUrlIdentity() + "/oauth/token?grant_type=client_credentials&client_id=" + getClientId() + "&client_secret=" + getClientSecret();

        try {
            String response = post(accessTokenUrl, 80, null, null);
            JSONObject jsonObject = new JSONObject(response);
            setClientToken(jsonObject.get("access_token").toString());
            setClientTokenType(jsonObject.get("token_type").toString());
            setClientTokenExpiresIn(Integer.parseInt(jsonObject.get("expires_in").toString()));
            setClientTokenRetrievedAt(System.currentTimeMillis());
        } catch (JSONException jsone) {
            log.warn("Response was not json object: " + jsone.getMessage());

        } catch (IOException ioe) {
            log.warn("Access token refresh failed: " + ioe.getMessage());
        }
    }

    public void deleteLeads(long[] leadIds) {
        //build our leads up
        String leads = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leadIds.length; i++) {
            long leadId = leadIds[i];
            if (i > 0)
                sb.append(",");
            sb.append(leadId);
            leads = sb.toString();
        }

        //create the final url
        String url = restUrlLeads + "?access_token=" + clientToken + "&_method=DELETE&id=" + leads;

        try {
            String result = post(url, 80, null, null);
        } catch (IOException ioe) {
            log.warn("Lead deletion had IO exception: " + ioe.getMessage());
        }
    }

    public String post(String url, int port, Map customHeaders, String entityContent) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            HttpPost post = new HttpPost(url + ":" + port);

            post.addHeader("Content-type", "application/json");
            post.addHeader("accept", "application/json");

            //add custom headers if any specified
            if (customHeaders != null) {
                Set<Map.Entry> setHeaders = customHeaders.entrySet();
                for (Iterator<Map.Entry> iterator = setHeaders.iterator(); iterator.hasNext(); ) {
                    Map.Entry header = iterator.next();
                    post.addHeader((String) header.getKey(), (String) header.getValue());
                }
            }

            HttpEntity entityOut = new StringEntity(entityContent != null ? entityContent : "");
            post.setEntity(entityOut);

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };

            String responseBody = httpClient.execute(post, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            return responseBody;

        } catch (ClientProtocolException cpe) {
            log.warn("XML post had client protocol exception, message was: " + cpe.getMessage());
            cpe.printStackTrace();
        } catch (IOException ioe) {
            log.warn("XML post had io exception, message was: " + ioe.getMessage());
            ioe.printStackTrace();
        } finally {
            httpClient.close();
        }

        return null;
    }
}
