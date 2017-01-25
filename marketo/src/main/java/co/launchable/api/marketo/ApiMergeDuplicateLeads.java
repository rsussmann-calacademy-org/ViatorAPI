package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 6/12/2015.
 */
@PropertySource("co.launchable.api.marketo.marketo.properties")
public class ApiMergeDuplicateLeads extends ApiBase {

    @Autowired
    Environment env;

    DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute() {
        super.initialize(env);

        try {
            List emails = new ArrayList();
            Connection con = dataSource.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT distinct key1 FROM MarketoStatus WHERE error like 'Found more%' and lastUpdated > dateadd(dd, -3, getdate())");
            while (rs.next()) {
                emails.add(rs.getString(1));
                mergeEmailToLast(rs.getString(1));
            }

            System.out.println(emails.size() + " emails found");
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mergeEmailToFirst(String email) {

        try {
            // Set Authentication Header
            AuthenticationHeader header = generateAuthenticationHeader();

            // Create Request
            ParamsGetMultipleLeads request = new ParamsGetMultipleLeads();

            LeadKeySelector keySelector = new LeadKeySelector();
            keySelector.setKeyType(LeadKeyRef.EMAIL);

            ArrayOfString aos = new ArrayOfString();
            aos.getStringItems().add(email);
            keySelector.setKeyValues(aos);
            request.setLeadSelector(keySelector);

            ArrayOfString attributes = new ArrayOfString();
            attributes.getStringItems().add("EmailAddress");
            request.setIncludeAttributes(attributes);

            SuccessGetMultipleLeads result = getMarketoWebServicePort().getMultipleLeads(request, header);
//            JAXBContext context = JAXBContext.newInstance(SuccessGetMultipleLeads.class);
//            Marshaller m = context.createMarshaller();
//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            m.marshal(result, System.out);

            ArrayOfLeadRecord arrayOfLeadRecord = result.getResult().getLeadRecordList().getValue();
            List<LeadRecord> leadRecords = arrayOfLeadRecord.getLeadRecords();

            Integer lowestLeadRecordId = null;
            LeadRecord lowestLeadRecord = null;
            for (int i = 0; i < leadRecords.size(); i++) {
                LeadRecord leadRecord = leadRecords.get(i);
                Integer leadRecordId = leadRecord.getId().getValue();
                if (lowestLeadRecordId == null || lowestLeadRecordId > leadRecordId) {
                    lowestLeadRecordId = leadRecordId;
                    lowestLeadRecord = leadRecord;
                }
            }

            ParamsMergeLeads requestMerge = new ParamsMergeLeads();
            ArrayOfAttribute winningLeadArray = new ArrayOfAttribute();

            Attribute winner = new Attribute();
            winner.setAttrName("IDNUM");
            winner.setAttrValue(lowestLeadRecordId.toString());
            winningLeadArray.getAttributes().add(winner);
            requestMerge.setWinningLeadKeyList(winningLeadArray);

            ArrayOfKeyList losingKeyList = new ArrayOfKeyList();

            for (int i = 0; i < leadRecords.size(); i++) {
                LeadRecord leadRecord = leadRecords.get(i);
                Integer leadRecordId = leadRecord.getId().getValue();
                if (!lowestLeadRecordId.equals(leadRecordId)) {
                    ArrayOfAttribute losingLeadArray = new ArrayOfAttribute();
                    Attribute loser = new Attribute();
                    loser.setAttrName("IDNUM");
                    loser.setAttrValue(leadRecordId.toString());
                    losingLeadArray.getAttributes().add(loser);
                    losingKeyList.getKeyLists().add(losingLeadArray);
                }
            }

            requestMerge.setLosingLeadKeyLists(losingKeyList);
            SuccessMergeLeads resultMerge = getMarketoWebServicePort().mergeLeads(requestMerge, header);
//            JAXBContext contextMerge = JAXBContext.newInstance(SuccessMergeLeads.class);
//            Marshaller mMerge = contextMerge.createMarshaller();
//            mMerge.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            mMerge.marshal(resultMerge, System.out);

            Integer winningId = resultMerge.getResult().getMergeStatus().getWinningLeadId().getValue();
            List<Integer> losingIds = resultMerge.getResult().getMergeStatus().getLosingLeadIdList().getValue().getIntegerItems();

            for (int i = 0; i < losingIds.size(); i++) {
                System.out.print(winningId);
                System.out.print(",");
                Integer losingId = losingIds.get(i);
                System.out.println(losingId);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void merge(int winnerLeadId, int[] losingLeadIds) {
        super.initialize(env);

        try {
            // Set Authentication Header
            AuthenticationHeader header = generateAuthenticationHeader();

            // Create Request
            ParamsGetMultipleLeads request = new ParamsGetMultipleLeads();

            LeadKeySelector keySelector = new LeadKeySelector();
            keySelector.setKeyType(LeadKeyRef.EMAIL);

            ParamsMergeLeads requestMerge = new ParamsMergeLeads();
            ArrayOfAttribute winningLeadArray = new ArrayOfAttribute();

            Attribute winner = new Attribute();
            winner.setAttrName("IDNUM");
            winner.setAttrValue(winnerLeadId + "");
            winningLeadArray.getAttributes().add(winner);
            requestMerge.setWinningLeadKeyList(winningLeadArray);

            ArrayOfKeyList losingKeyList = new ArrayOfKeyList();

            for (int i = 0; i < losingLeadIds.length; i++) {
                int loserId = losingLeadIds[i];

                if (loserId != winnerLeadId) {
                    ArrayOfAttribute losingLeadArray = new ArrayOfAttribute();
                    Attribute loser = new Attribute();
                    loser.setAttrName("IDNUM");
                    loser.setAttrValue(loserId + "");
                    losingLeadArray.getAttributes().add(loser);
                    losingKeyList.getKeyLists().add(losingLeadArray);
                }
            }

            requestMerge.setLosingLeadKeyLists(losingKeyList);
            SuccessMergeLeads resultMerge = getMarketoWebServicePort().mergeLeads(requestMerge, header);

            Integer winningId = resultMerge.getResult().getMergeStatus().getWinningLeadId().getValue();
            List<Integer> losingIds = resultMerge.getResult().getMergeStatus().getLosingLeadIdList().getValue().getIntegerItems();

            for (int i = 0; i < losingIds.size(); i++) {
                System.out.print(winningId);
                System.out.print(",");
                Integer losingId = losingIds.get(i);
                System.out.println(losingId);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void mergeEmailToLast(String email) {

        try {
            // Set Authentication Header
            AuthenticationHeader header = generateAuthenticationHeader();

            // Create Request
            ParamsGetMultipleLeads request = new ParamsGetMultipleLeads();

            LeadKeySelector keySelector = new LeadKeySelector();
            keySelector.setKeyType(LeadKeyRef.EMAIL);

            ArrayOfString aos = new ArrayOfString();
            aos.getStringItems().add(email);
            keySelector.setKeyValues(aos);
            request.setLeadSelector(keySelector);

            ArrayOfString attributes = new ArrayOfString();
            attributes.getStringItems().add("EmailAddress");
            request.setIncludeAttributes(attributes);

            SuccessGetMultipleLeads result = getMarketoWebServicePort().getMultipleLeads(request, header);

            ArrayOfLeadRecord arrayOfLeadRecord = result.getResult().getLeadRecordList().getValue();
            List<LeadRecord> leadRecords = arrayOfLeadRecord.getLeadRecords();

            Integer highestLeadRecordId = null;
            LeadRecord highestLeadRecord = null;
            for (int i = 0; i < leadRecords.size(); i++) {
                LeadRecord leadRecord = leadRecords.get(i);
                Integer leadRecordId = leadRecord.getId().getValue();
                if (highestLeadRecordId == null || highestLeadRecordId < leadRecordId) {
                    highestLeadRecordId = leadRecordId;
                    highestLeadRecord = leadRecord;
                }
            }

            ParamsMergeLeads requestMerge = new ParamsMergeLeads();
            ArrayOfAttribute winningLeadArray = new ArrayOfAttribute();

            Attribute winner = new Attribute();
            winner.setAttrName("IDNUM");
            winner.setAttrValue(highestLeadRecordId.toString());
            winningLeadArray.getAttributes().add(winner);
            requestMerge.setWinningLeadKeyList(winningLeadArray);

            ArrayOfKeyList losingKeyList = new ArrayOfKeyList();

            for (int i = 0; i < leadRecords.size(); i++) {
                LeadRecord leadRecord = leadRecords.get(i);
                Integer leadRecordId = leadRecord.getId().getValue();
                if (!highestLeadRecordId.equals(leadRecordId)) {
                    ArrayOfAttribute losingLeadArray = new ArrayOfAttribute();
                    Attribute loser = new Attribute();
                    loser.setAttrName("IDNUM");
                    loser.setAttrValue(leadRecordId.toString());
                    losingLeadArray.getAttributes().add(loser);
                    losingKeyList.getKeyLists().add(losingLeadArray);
                }
            }

            requestMerge.setLosingLeadKeyLists(losingKeyList);
            SuccessMergeLeads resultMerge = getMarketoWebServicePort().mergeLeads(requestMerge, header);

            Integer winningId = resultMerge.getResult().getMergeStatus().getWinningLeadId().getValue();
            List<Integer> losingIds = resultMerge.getResult().getMergeStatus().getLosingLeadIdList().getValue().getIntegerItems();

            for (int i = 0; i < losingIds.size(); i++) {
                System.out.print(winningId);
                System.out.print(",");
                Integer losingId = losingIds.get(i);
                System.out.println(losingId);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        try {
            ApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/co.launchable.api.marketo.marketo-dispatcher-servlet.xml");
            ApiMergeDuplicateLeads apiMergeDuplicateLeads = (ApiMergeDuplicateLeads)ctx.getBean("apiMergeDuplicateLeads");
            ServiceMarketo serviceMarketo = (ServiceMarketo)ctx.getBean("serviceMarketo");

            apiMergeDuplicateLeads.execute();
            //apiGetMultipleLeads.merge(4546906, new int[] {1206734});
            //serviceMarketo.syncPendingConstituents(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
