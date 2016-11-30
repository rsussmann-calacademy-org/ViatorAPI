package co.launchable.api.marketo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


public class ApiGetDeletedLeads {
    public String marketoInstance = "https://945-SMH-086.mktorest.com";//Replace this with the host from Admin Web Services
    public String marketoIdURL = marketoInstance + "/identity";
    public String clientId = "3ef87869-8817-4145-8a29-f09e722555fb";	//Obtain from your Custom Service in Admin>Launchpoint
    public String clientSecret = "I8FHT0vhIuCRRT9EtDxTDEyDjFQcHCLF";	//Obtain from your Custom Service in Admin>Launchpoint
    public String idEndpoint = marketoIdURL + "/oauth/token?grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;
    public String nextPageToken;//paging token retrieved with get paging token
    public Integer batchSize; //max 300, default 300
    public String sinceDateTime = "2015-10-01T00:00:00z";
    private SimpleDateFormat sdfSend = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'z'");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private String sqlInsertDeletion = "INSERT INTO MarketoDeletions (leadId, deletionDate) VALUES (?, ?)";
    private PreparedStatement pstmtDeletion;
    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args){
        ApplicationContext ctx = new FileSystemXmlApplicationContext("//Users/mike/Dropbox/Projects/ASViatorIntegration/src/main/webapp/WEB-INF/marketo-dispatcher-servlet.xml");
        ServiceMarketo serviceMarketo = (ServiceMarketo)ctx.getBean("serviceMarketo");
        serviceMarketo.executeDailyProcesses();
    }

    public void process() {
        List listDeletions = new ArrayList();

//        Calendar cal = new GregorianCalendar();
//        cal.add(Calendar.DATE, -1);
//
//        java.util.Date yesterdayDate = cal.getTime();
//        sinceDateTime = sdfSend.format(yesterdayDate);

        try {
            Connection con = dataSource.getConnection();

            JsonObject deletions;

            String nextToken = this.getPagingToken();
            while (!nextToken.equals("")) {
                this.nextPageToken = nextToken;
                deletions = this.getDeletions();

                JsonValue jsonResult = deletions.get("result");
                if (jsonResult != null) {
                    JsonArray items = deletions.get("result").asArray();
                    for (JsonValue item : items) {
                        listDeletions.add(item);
                    }
                }

                if (listDeletions.size() > 0) {
                    this.insertDeletions(listDeletions, con);
                    listDeletions.clear();
                }

                nextToken = deletions.getString("nextPageToken", "");
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        System.out.println(listDeletions.size() + " deletions found");
    }

    private Date convertDate(String input) {
        if (input == null)
            return null;

        try {
            java.util.Date date = sdf.parse(input);
            return new java.sql.Date(date.getTime());
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return null;
    }

    private void insertDeletions(List listDeletions, Connection con) {

        int count = 0;
        try {
            if (pstmtDeletion == null)
                pstmtDeletion = con.prepareStatement(sqlInsertDeletion);

            for (int i = 0; i < listDeletions.size(); i++) {
                JsonObject jsonObject = (JsonObject) listDeletions.get(i);
                pstmtDeletion.clearParameters();

                int leadId = jsonObject.getInt("leadId", -1);
                Date activityDate = convertDate(jsonObject.getString("activityDate", null));

                if (leadId != -1) {
                    pstmtDeletion.setInt(1, leadId);
                    pstmtDeletion.setDate(2, activityDate);
                    count += pstmtDeletion.executeUpdate();
                }
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        System.out.println(count + " deletions inserted");
    }

    //make request
    private String getPagingToken() {
        String pagingToken = null;
        try {
            //assemble the URL
            StringBuilder endpoint = new StringBuilder(marketoInstance + "/rest/v1/activities/pagingtoken.json?access_token=" + getToken() + "&sinceDatetime=" + sinceDateTime);
            URL url = new URL(endpoint.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty("accept", "text/json");
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();

                Reader reader = new InputStreamReader(inStream);
                JsonObject jsonObject = JsonObject.readFrom(reader);
                pagingToken = jsonObject.get("nextPageToken").asString();
            } else {
                pagingToken = null;
            }
        } catch (MalformedURLException e) {
            System.out.println("URL not valid.");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
        return pagingToken;
    }

    //Make Request
    private JsonObject getDeletions() {
        JsonObject jsonObject = null;
        try {
            //Assemble the URL
            StringBuilder endpoint = new StringBuilder(marketoInstance + "/rest/v1/activities/deletedleads.json?access_token=" + getToken());
            endpoint.append("&nextPageToken=" + nextPageToken);
            //add optional params
            if (batchSize != null && batchSize >0 && batchSize <= 300){
                endpoint.append("&batchSize=" + batchSize);
            }
            URL url = new URL(endpoint.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty("accept", "text/json");
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                Reader reader = new InputStreamReader(inStream);
                jsonObject = JsonObject.readFrom(reader);
            } else {
                jsonObject = null;
            }
        } catch (MalformedURLException e) {
            System.out.println("URL not valid.");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String postData(){
        String result = null;
        try {
            JsonObject requestBody = buildRequest();
            String endpoint = marketoInstance + "/rest/v1/leads.json?access_token=" + getToken();
            URL url = new URL(endpoint.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("accept", "text/json");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write(requestBody.toString());
            wr.flush();
            int responseCode = urlConn.getResponseCode();

            if (responseCode == 200){
                InputStream inStream = urlConn.getInputStream();
                result = convertStreamToString(inStream);
            }else{
                result = "Status Code: " + responseCode;
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;

    }
    public String getToken(){
        String token = null;
        try {
            URL url = new URL(idEndpoint);
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty("accept", "application/json");
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                Reader reader = new InputStreamReader(inStream);
                JsonObject jsonObject = JsonObject.readFrom(reader);
                token = jsonObject.get("access_token").asString();
            }else {
                throw new IOException("Status: " + responseCode);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }

    private JsonObject buildRequest(){
        return new JsonObject();
    }

    private String convertStreamToString(InputStream inputStream) {

        try {
            return new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}