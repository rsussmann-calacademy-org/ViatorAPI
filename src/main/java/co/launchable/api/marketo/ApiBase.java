package co.launchable.api.marketo;

import com.marketo.mktows.AuthenticationHeader;
import com.marketo.mktows.MktMktowsApiService;
import com.marketo.mktows.MktowsPort;
import org.apache.commons.codec.binary.Hex;
import org.springframework.core.env.Environment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by michaelmcelligott on 1/17/14.
 */
public class ApiBase {
    protected String prefix = "marketo.";
    private String marketoEndpoint = "CHANGE ME" + "?WDSL";
    private String marketoUserId = "CHANGE ME";
    private String marketoSecretKey = "CHANGE ME";
    private String marketoServiceQnameUrl = "http://www.marketo.com/mktows/";
    private String marketoServiceQnameName = "MktMktowsApiService";
    protected DataSource dataSource;
    private String dataSourceDriver;
    private String dataSourceUrl;
    private String dataSourceUser;
    private String dataSourcePassword;
    private String columnLastUploaded = null;
    private String batchRows = "0";
    protected int rowsProcessed = 0;
    private int fullRowsProcessed = 0;
    protected Connection con;
    protected PreparedStatement pstmt;
    private MktowsPort marketoWebServicePort;
    private boolean readOnlyRead = true;

    public boolean isReadOnlyRead() {
        return readOnlyRead;
    }

    public void setReadOnlyRead(boolean readOnlyRead) {
        this.readOnlyRead = readOnlyRead;
    }

    public void initialize(Environment env) {
        setMarketoEndpoint(env.getProperty(prefix + "marketoEndpoint"));
        setMarketoUserId(env.getProperty(prefix + "marketoUserId"));
        setMarketoSecretKey(env.getProperty(prefix + "marketoSecretKey"));
        setMarketoServiceQnameUrl(env.getProperty(prefix + "marketoQnameUrl"));
        setMarketoServiceQnameName(env.getProperty(prefix + "marketoQnameName"));
        setDataSourceDriver(env.getProperty(prefix + "dataSourceDriver"));
        setDataSourceUrl(env.getProperty(prefix + "dataSourceUrl"));
        setDataSourceUser(env.getProperty(prefix + "dataSourceUser"));
        setDataSourcePassword(env.getProperty(prefix + "dataSourcePassword"));
        setBatchRows(env.getProperty(prefix + "batchRows"));
        setColumnLastUploaded(env.getProperty(prefix + "columnLastUploaded"));
    }

    protected void sleepThroughDeadlock() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            //just ignore this.. we're only adding a delay to allow the deadlock condition
            //to clear
        }
    }

    public MktowsPort getMarketoWebServicePort() {
        return marketoWebServicePort;
    }

    public void setMarketoWebServicePort(MktowsPort marketoWebServicePort) {
        this.marketoWebServicePort = marketoWebServicePort;
    }

    public String getSql() {
        return "";
    }

    public String getColumnLastUploaded() {
        return columnLastUploaded;
    }

    public void setColumnLastUploaded(String columnLastUploaded) {
        this.columnLastUploaded = columnLastUploaded;
    }

    public String getBatchRows() {
        return batchRows;
    }

    public void setBatchRows(String batchRows) {
        this.batchRows = batchRows;
    }

    public String getMarketoEndpoint() {
        return marketoEndpoint;
    }

    public void setMarketoEndpoint(String marketoEndpoint) {
        this.marketoEndpoint = marketoEndpoint;
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

    public String getDataSourceDriver() {
        return dataSourceDriver;
    }

    public void setDataSourceDriver(String dataSourceDriver) {
        this.dataSourceDriver = dataSourceDriver;
    }

    public String getDataSourceUrl() {
        return dataSourceUrl;
    }

    public void setDataSourceUrl(String dataSourceUrl) {
        this.dataSourceUrl = dataSourceUrl;
    }

    public String getDataSourceUser() {
        return dataSourceUser;
    }

    public void setDataSourceUser(String dataSourceUser) {
        this.dataSourceUser = dataSourceUser;
    }

    public String getDataSourcePassword() {
        return dataSourcePassword;
    }

    public void setDataSourcePassword(String dataSourcePassword) {
        this.dataSourcePassword = dataSourcePassword;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected boolean setupDatabase() {
        try {
            if (dataSource != null) {
                con = dataSource.getConnection();
                if (getSql() != null)
                    pstmt = con.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            } else {
                DriverManager.registerDriver((Driver) Class.forName(dataSourceDriver).newInstance());
                con = DriverManager.getConnection(dataSourceUrl, dataSourceUser, dataSourcePassword);

    //            if (!readOnlyRead) {
    //                pstmt = con.prepareStatement(getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    //                con.setAutoCommit(false);
    //            }
    //            else
                    pstmt = con.prepareStatement(getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

    //            if (!"0".equals(batchRows))
    //                pstmt.setMaxRows(Integer.parseInt(batchRows));
                return true;
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (InstantiationException ie) {
            ie.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return false;
    }

    protected void releaseDatabase() {
        //try {
        //    if (pstmt != null && !pstmt.isClosed()) {
        //        pstmt.close();
        //    }
        //} catch (SQLException sqle1) {
        //    sqle1.printStackTrace();
        //}

        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException sqle2) {
            sqle2.printStackTrace();
        }
    }

    protected ResultSet retrieveResultSet(String sql) throws SQLException {
        if (con != null && !con.isClosed() && pstmt != null) {
            ResultSet rs = pstmt.executeQuery();
            return rs;
        }
        return null;
    }

    protected AuthenticationHeader generateAuthenticationHeader() {
        try {
            URL marketoSoapEndPoint = new URL(getMarketoEndpoint());

            QName serviceName = new QName(marketoServiceQnameUrl, marketoServiceQnameName);
            MktMktowsApiService service = new MktMktowsApiService(marketoSoapEndPoint, serviceName);
            marketoWebServicePort = service.getMktowsApiSoapPort();

            // Create Signature
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            String text = df.format(new Date());
            String requestTimestamp = text.substring(0, 22) + ":" + text.substring(22);
            String encryptString = requestTimestamp + marketoUserId ;

            SecretKeySpec secretKey = new SecretKeySpec(marketoSecretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(encryptString.getBytes());
            char[] hexChars = Hex.encodeHex(rawHmac);
            String signature = new String(hexChars);

            // Set Authentication Header
            AuthenticationHeader header = new AuthenticationHeader();
            header.setMktowsUserId(marketoUserId);
            header.setRequestTimestamp(requestTimestamp);
            header.setRequestSignature(signature);
            return header;
        } catch (Exception e) {
                e.printStackTrace();
        }
        return null;
    }
}
