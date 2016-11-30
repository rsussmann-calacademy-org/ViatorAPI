package co.launchable.api.academy;

import org.springframework.core.env.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * Created by michaelmcelligott on 1/28/14.
 */
public class ApiSaveSubscriptionProfile {
    private String prefix = "academy.";
    private boolean connectionLocal = false;
    private Connection connection;
    private DataSource dataSource;
    private String sqlSelectContact;
    private String sqlInsertContact;
    private String sqlUpdateContact;
    private String sqlInsertInterest;
    private String sqlSelectInterestCodes;

    private Map parameterMap;
    private List interestsRequested = new ArrayList();
    private List interestsRegistered = new ArrayList();
    private Long contactId;
    private Set interestCodes = new HashSet();

    public String getSqlSelectInterestCodes() {
        return sqlSelectInterestCodes;
    }

    public void setSqlSelectInterestCodes(String sqlSelectInterestCodes) {
        this.sqlSelectInterestCodes = sqlSelectInterestCodes;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public List getInterestsRegistered() {
        return interestsRegistered;
    }

    public void setInterestsRegistered(List interestsRegistered) {
        this.interestsRegistered = interestsRegistered;
    }

    public List getInterestsRequested() {
        return interestsRequested;
    }

    public void setInterestsRequested(List interestsRequested) {
        this.interestsRequested = interestsRequested;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getSqlInsertInterest() {
        return sqlInsertInterest;
    }

    public void setSqlInsertInterest(String sqlInsertInterest) {
        this.sqlInsertInterest = sqlInsertInterest;
    }

    public String getSqlInsertContact() {
        return sqlInsertContact;
    }

    public void setSqlInsertContact(String sqlInsertContact) {
        this.sqlInsertContact = sqlInsertContact;
    }

    public String getSqlSelectContact() {
        return sqlSelectContact;
    }

    public void setSqlSelectContact(String sqlSelectContact) {
        this.sqlSelectContact = sqlSelectContact;
    }

    public String getSqlUpdateContact() {
        return sqlUpdateContact;
    }

    public void setSqlUpdateContact(String sqlUpdateContact) {
        this.sqlUpdateContact = sqlUpdateContact;
    }

    public void initFromEnvironment(Environment environment) {
        setSqlSelectContact(environment.getProperty(prefix + "sqlSelectContact"));
        setSqlUpdateContact(environment.getProperty(prefix + "sqlUpdateContact"));
        setSqlInsertContact(environment.getProperty(prefix + "sqlInsertContact"));
        setSqlInsertInterest(environment.getProperty(prefix + "sqlInsertInterest"));
        setSqlSelectInterestCodes(environment.getProperty(prefix + "sqlSelectInterestCodes"));
    }

    private void submitInterest(String code, Long contactId, PreparedStatement pstmt) throws SQLException {
        try {
            pstmt.setLong(1, contactId);
            pstmt.setString(2, code);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 1)
                interestsRegistered.add(code);
        } catch (SQLException sqle) {
            String message = sqle.getMessage();
            if (message.indexOf("Cannot insert duplicate") > 0) {
                interestsRegistered.add(code);
            } else
                throw sqle;
        }
    }

    private Long getContactIdForEmail(String email, Connection con) throws SQLException {
        Long contactId = null;
        PreparedStatement pstmtSelectContact = con.prepareStatement(getSqlSelectContact());
        pstmtSelectContact.setString(1, (String)parameterMap.get("email"));
        ResultSet rsSelect = pstmtSelectContact.executeQuery();
        if (rsSelect.next())
            contactId = rsSelect.getLong(1);
        return contactId;
    }

    private Long insertContact(Connection con) throws SQLException {
        Long contactId = null;
        PreparedStatement pstmtInsertContact = con.prepareStatement(getSqlInsertContact(), Statement.RETURN_GENERATED_KEYS);

        pstmtInsertContact.setString(1, (String)parameterMap.get("firstname"));
        pstmtInsertContact.setString(2, (String)parameterMap.get("lastname"));
        pstmtInsertContact.setString(3, (String)parameterMap.get("email"));
        pstmtInsertContact.setString(4, (String)parameterMap.get("phone"));
        pstmtInsertContact.setString(5, (String)parameterMap.get("source"));
        pstmtInsertContact.executeUpdate();

        ResultSet rsKeys = pstmtInsertContact.getGeneratedKeys();
        while (rsKeys.next())
            contactId = rsKeys.getLong(1);
        return contactId;
    }

    private int updateContact(Long contactId, Connection con) throws SQLException {
        PreparedStatement pstmtUpdateContact = con.prepareStatement(getSqlUpdateContact());
        pstmtUpdateContact.setString(1, (String)parameterMap.get("firstname"));
        pstmtUpdateContact.setString(2, (String)parameterMap.get("lastname"));
        pstmtUpdateContact.setString(3, (String)parameterMap.get("phone"));
        pstmtUpdateContact.setString(4, (String)parameterMap.get("email"));
        return pstmtUpdateContact.executeUpdate();
    }

    private Long updateOrCreateContact(Connection con) throws SQLException {
        Long contactId = getContactIdForEmail((String) parameterMap.get("email"), con);
        if (contactId == null)
            contactId = insertContact(con);
        else
            updateContact(contactId, con);
        return contactId;
    }

    private void loadInterestCodes(Connection con) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(getSqlSelectInterestCodes());
        while (rs.next()) {
            interestCodes.add(rs.getString(1));
        }
    }

    public void run() throws Exception {
        Connection con = null;

        try {
            con = getConnection();
            loadInterestCodes(con);
            contactId = updateOrCreateContact(con);

            if (parameterMap.containsKey("interests")) {
                PreparedStatement pstmtInterest = con.prepareStatement(sqlInsertInterest);

                String[] interests = ((String)parameterMap.get("interests")).split(",");
                for (int i = 0; i < interests.length; i++) {
                    String interest = interests[i].trim();
                    interestsRequested.add(interest);
                    if (interestCodes.contains(interest))
                        submitInterest(interests[i], contactId, pstmtInterest);
                }
            }

            if (!con.getAutoCommit())
                con.commit();
        } catch (Exception e) {
            //e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException sqle2) {
                    sqle2.printStackTrace();
                }
            }
            throw e;
        } finally {
            releaseConnection();
        }
    }

    private void releaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException sqle3) {
            sqle3.printStackTrace();
        }
    }

    public Connection getConnection() throws Exception {
        if (dataSource != null) {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } else
            return null;
    }
}
