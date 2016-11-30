package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.apache.commons.codec.binary.Hex;
import org.springframework.core.env.Environment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by michaelmcelligott on 1/15/14.
 */
public class ApiImportToList {
    private String prefix = "marketo.";
    private String marketoEndpoint = "CHANGE ME" + "?WDSL";
    private String marketoUserId = "CHANGE ME";
    private String marketoSecretKey = "CHANGE ME";
    private String marketoServiceQnameUrl = "http://www.marketo.com/mktows/";
    private String marketoServiceQnameName = "MktMktowsApiService";
    private String marketoProgramName = "Trav-Demo-Program";
    private String marketoCampaignName = "Batch Campaign Example";
    private String marketoListName = "Trav-Test-List";
    private String columnNames = "Last Name,First Name,Job Title,Company Name,Email Address";
    private String columnIndices = null;
    private String useSqlColumnNames = "false";
    private String sql = "";
    private String columnListName = null;
    private String columnCampaignName = null;
    private String columnProgramName = null;
    private String columnLastUploaded = null;
    private String dataSourceDriver;
    private String dataSourceUrl;
    private String dataSourceUser;
    private String dataSourcePassword;
    private String batchRows = "0"; //send them all
    private int rowsProcessed = 0;
    private int fullRowsProcessed = 0;

    private Connection con;
    private PreparedStatement pstmt;
    StringBuilder sb = new StringBuilder();

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
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

    public String getMarketoProgramName() {
        return marketoProgramName;
    }

    public void setMarketoProgramName(String marketoProgramName) {
        this.marketoProgramName = marketoProgramName;
    }

    public String getMarketoCampaignName() {
        return marketoCampaignName;
    }

    public void setMarketoCampaignName(String marketoCampaignName) {
        this.marketoCampaignName = marketoCampaignName;
    }

    public String getMarketoListName() {
        return marketoListName;
    }

    public void setMarketoListName(String marketoListName) {
        this.marketoListName = marketoListName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getUseSqlColumnNames() {
        return useSqlColumnNames;
    }

    public void setUseSqlColumnNames(String useSqlColumnNames) {
        this.useSqlColumnNames = useSqlColumnNames;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getColumnListName() {
        return columnListName;
    }

    public void setColumnListName(String columnListName) {
        this.columnListName = columnListName;
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

    public String getBatchRows() {
        return batchRows;
    }

    public void setBatchRows(String batchRows) {
        this.batchRows = batchRows;
    }

    public String getColumnCampaignName() {
        return columnCampaignName;
    }

    public void setColumnCampaignName(String columnCampaignName) {
        this.columnCampaignName = columnCampaignName;
    }

    public String getColumnProgramName() {
        return columnProgramName;
    }

    public void setColumnProgramName(String columnProgramName) {
        this.columnProgramName = columnProgramName;
    }

    public String getColumnLastUploaded() {
        return columnLastUploaded;
    }

    public void setColumnLastUploaded(String columnLastUploaded) {
        this.columnLastUploaded = columnLastUploaded;
    }

    public String getColumnIndices() {
        return columnIndices;
    }

    public void setColumnIndices(String columnIndices) {
        this.columnIndices = columnIndices;
    }

    public void initFromEnvironment(Environment environment) {
        setMarketoEndpoint(environment.getProperty(prefix + "marketoEndpoint"));
        setMarketoUserId(environment.getProperty(prefix + "marketoUserId"));
        setMarketoSecretKey(environment.getProperty(prefix + "marketoSecretKey"));
        setMarketoServiceQnameUrl(environment.getProperty(prefix + "marketoQnameUrl"));
        setMarketoServiceQnameName(environment.getProperty(prefix + "marketoQnameName"));
        setMarketoProgramName(environment.getProperty(prefix + "marketoProgramName"));
        setMarketoCampaignName(environment.getProperty(prefix + "marketoCampaignName"));
        setMarketoListName(environment.getProperty(prefix + "marketoListName"));
        setDataSourceDriver(environment.getProperty(prefix + "dataSourceDriver"));
        setDataSourceUrl(environment.getProperty(prefix + "dataSourceUrl"));
        setDataSourceUser(environment.getProperty(prefix + "dataSourceUser"));
        setDataSourcePassword(environment.getProperty(prefix + "dataSourcePassword"));

        setSql(environment.getProperty(prefix + "sql"));
        setColumnListName(environment.getProperty(prefix + "columnListName"));
        setColumnCampaignName(environment.getProperty(prefix + "columnCampaignName"));
        setColumnProgramName(environment.getProperty(prefix + "columnProgramName"));
        setBatchRows(environment.getProperty(prefix + "batchRows"));
        setColumnNames(environment.getProperty(prefix + "columnNames"));
        setColumnIndices(environment.getProperty(prefix + "columnIndices"));
        setUseSqlColumnNames(environment.getProperty(prefix + "useSqlColumnNames"));
    }

    public boolean initialize() {

        try {
            DriverManager.registerDriver((Driver)Class.forName(dataSourceDriver).newInstance());
            con = DriverManager.getConnection(dataSourceUrl, dataSourceUser, dataSourcePassword);

            if (columnLastUploaded != null) {
                pstmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                con.setAutoCommit(false);
            }
            else
                pstmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            if (!"0".equals(batchRows))
                pstmt.setMaxRows(Integer.parseInt(batchRows));
            return true;
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

    public int processAllRows() {
        rowsProcessed = processNextRows();

//        do {
//            rowsProcessed = processNextRows();
//            fullRowsProcessed += rowsProcessed;
//        } while (fullRowsProcessed < 5000);
        return rowsProcessed;
    }

    private int processNextRows() {
        rowsProcessed = 0;
        String[] indicesAsStringArray = columnIndices.split(",");
        int[] indices = new int[indicesAsStringArray.length];

        for (int i = 0; i < indicesAsStringArray.length; i++) {
            indices[i] = Integer.parseInt(indicesAsStringArray[i]);
        }

        Map<String, List> mapListNameToListOfRows = new HashMap<String, List>();
        Map<String, List> mapListNameToRowNumbers = new HashMap<String, List>();

        try {
            if (con != null && !con.isClosed() && pstmt != null) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next())
                    processRow(rs, indices, mapListNameToListOfRows, mapListNameToRowNumbers);

                Map mapListNameToSubmissionResult = submitAllRows(mapListNameToListOfRows);
                Set transferResultEntries = mapListNameToSubmissionResult.entrySet();

                //if we have a "lastUploaded" column configured, update that column where appropriate
                if (columnLastUploaded != null) {
                    processTransferResults(transferResultEntries, mapListNameToRowNumbers, rs);
                    //after we've iterated over the whole map we've updated all rows that were actually
                    //successfully posted, so we can commit the transaction
                    con.commit();
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return rowsProcessed;
    }

    //process the result of posting all of these documents to Marketo
    //each entry in mapListNameToSubmissionResult has something like:
    //  "Nightlife Subscribers" -> true
    //We want to iterate over the whole map so we hit every list
    //and if the post was successful, update the "lastUploaded" column
    //with the current date for the list in question
    private void processTransferResults(Set transferResults, Map<String, List> mapListNameToRowNumbers, ResultSet rs) throws SQLException {
        Iterator it = transferResults.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            Boolean listWasPostedSuccessfully = (Boolean)entry.getValue();

            //if resultValue == true then the list in question (name is in entry.getKey())
            //was successfully posted to Marketo.  We want to update all of the rows
            //from our resultset that were a part of that particular list.  The set of
            //relevant rows are the value side of the map in mapListNameToRowNumbers, so, as an example:
            //entry.getKey() == "Nightlife Subscribers"
            //entry.getValue() == [1, 7, 8, 12, 17, 23,..]  <-- row ids from our resultset
            if (listWasPostedSuccessfully) {
                String listThatWasPosted = (String)entry.getKey();
                List rowNumbersForThatList = mapListNameToRowNumbers.get(listThatWasPosted);

                for (int i = 0; i < rowNumbersForThatList.size(); i++) {
                    int index = (Integer) rowNumbersForThatList.get(i);
                    rs.absolute(index);
                    rs.updateDate(columnLastUploaded, new java.sql.Date(new Date().getTime()));
                }
            }
        }
    }

    private void processRow(ResultSet rs, int[] indices, Map<String, List> mapListNameToListOfRows, Map<String, List> mapListNameToRowNumbers) throws SQLException {
        sb.setLength(0);
        for (int i = 0; i < indices.length; i++) {
            Object o = rs.getObject(indices[i]);
            if (o != null)
                sb.append(o.toString());
            if (i + 1 < indices.length)
                sb.append(',');
        }

        String row = sb.toString();
        String list = rs.getString(columnListName);

        //see if the list already exists, create it if it doesn't, append to it in
        //either case
        List targetList = mapListNameToListOfRows.get(list);
        List targetRowNumberList = mapListNameToRowNumbers.get(list);
        if (targetList == null) {
            targetList = new ArrayList();
            mapListNameToListOfRows.put(list, targetList);
            targetRowNumberList = new ArrayList();
            mapListNameToRowNumbers.put(list, targetRowNumberList);
        }
        targetList.add(row);
        targetRowNumberList.add(rs.getRow());
        rowsProcessed++;
    }

    //Incoming map mapListNameToListOfRows has entries like:
    //"Nightlife Subscribers" --> List<"col1value,col2value,col3value">
    //
    //Iterate over the map entries and post the List (map value) for each list name (map key)
    //Return a map from list name --> true|false indicating success of the post to Marketo
    private Map<String, Boolean> submitAllRows(Map<String, List> mapListNameToListOfRows) {
        Map<String, Boolean> mapResults = new HashMap<String, Boolean>();

        Set entries = mapListNameToListOfRows.entrySet();
        Iterator<Map.Entry<String, List>> it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry<String, List> entry = it.next();
            ArrayOfString rows = new ArrayOfString();
            rows.getStringItems().addAll(entry.getValue());

            String listName = entry.getKey();
            mapResults.put(entry.getKey(), submitListRows(listName, null, null, rows));
        }

        return mapResults;
    }

    private boolean submitListRows(String listName, String programName, String campaignName, ArrayOfString rows) {
        try {
            URL marketoSoapEndPoint = new URL(marketoEndpoint);

            QName serviceName = new QName(marketoServiceQnameUrl, marketoServiceQnameName);
            MktMktowsApiService service = new MktMktowsApiService(marketoSoapEndPoint, serviceName);
            MktowsPort port = service.getMktowsApiSoapPort();

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

            if (marketoProgramName != null && marketoCampaignName != null && listName != null) {
                // Create Request
                ParamsImportToList request = new ParamsImportToList();

                //@todo complete support for program name and campaign name, currently unsupported
                //@todo but will require a change to the code that does the mappings above (that is
                //@todo we'll probably need an Object -> List map instead of String -> List, which will
                //@todo complicate things a bit (but potentially also clarify)
                request.setProgramName(programName != null ? programName : marketoProgramName);
                //request.setCampaignName(campaignName != null ? campaignName : marketoCampaignName);
                request.setImportFileHeader(columnNames);

                //create our datasource and extract a set of rows, optionally sending them over in batches
                //of X
                //rows.getStringItems().add("Awesomesauce,Developer,Code Slinger,Marketo,dawesomesauce@marketo.com");
                //rows.getStringItems().add("Doe,Jane,VP Marketing,Jane Consulting,jdoe@janeconsulting.com");
                request.setImportFileRows(rows);
                request.setImportListMode(ImportToListModeEnum.UPSERTLEADS);
                request.setListName(listName);
                request.setClearList(true);

                SuccessImportToList result = port.importToList(request, header);
                result.getResult().getImportStatus().value();

                JAXBContext context = JAXBContext.newInstance(SuccessImportToList.class);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                m.marshal(result, System.out);
            }
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
