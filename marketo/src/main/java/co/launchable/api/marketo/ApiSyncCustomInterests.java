package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.springframework.core.env.Environment;

import javax.xml.bind.JAXBElement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by michaelmcelligott on 1/23/14.
 */
public class ApiSyncCustomInterests extends ApiSyncObjectBase {
    private String prefixLocal = "co.launchable.api.marketo.marketo.syncInterests.";
    private String objectTypeName = "Interest";
    private String[] attributes;

    public String getObjectTypeName() {
        return objectTypeName;
    }

    public void setObjectTypeName(String objectTypeName) {
        this.objectTypeName = objectTypeName;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public ApiSyncCustomInterests(Environment env) {
        attributes = ((String)env.getProperty(getPrefixLocal() + "attributes")).split(",");
        super.initialize(env);
    }

    public String getPrefixLocal() {
        return prefixLocal;
    }

    private void appendCustomObject(String emailAddress, Set interests, ArrayOfCustomObj customObjects) {
        //we have the full set of interests and the set for this email address, so build our object
        CustomObj customObj = new CustomObj();

        //set up key attributes
        ArrayOfAttribute arrayOfKeyAttributes = new ArrayOfAttribute();
        arrayOfKeyAttributes.getAttributes().add(buildAttribute("EmailAddress", emailAddress));
        arrayOfKeyAttributes.getAttributes().add(buildAttribute("InterestEmailAddress", emailAddress));

        JAXBElement<ArrayOfAttribute> keyAttributes = new ObjectFactory().createCustomObjCustomObjKeyList(arrayOfKeyAttributes);
        customObj.setCustomObjKeyList(keyAttributes);

        ArrayOfAttribute arrayOfValueAttributes = new ArrayOfAttribute();
        for (int i = 0; i < attributes.length; i++) {
            if (interests.contains(attributes[i]))
                arrayOfValueAttributes.getAttributes().add(buildAttribute(attributes[i], "true"));
            else
                arrayOfValueAttributes.getAttributes().add(buildAttribute(attributes[i], "false"));
        }

        JAXBElement<ArrayOfAttribute> valueAttributes = new ObjectFactory().createCustomObjCustomObjAttributeList(arrayOfValueAttributes);
        customObj.setCustomObjAttributeList(valueAttributes);
        customObjects.getCustomObjs().add(customObj);
    }

    protected ParamsSyncCustomObjects buildRequest(ResultSet rs) {
        ParamsSyncCustomObjects request = new ParamsSyncCustomObjects();
        request.setObjTypeName(objectTypeName);
        JAXBElement<SyncOperationEnum> operation = new ObjectFactory().createParamsSyncCustomObjectsOperation(SyncOperationEnum.UPSERT);
        request.setOperation(operation);

        ArrayOfCustomObj customObjects = new ArrayOfCustomObj();

        try {
            //not sure we'll need the metadata yet
            ResultSetMetaData rsmd = rs.getMetaData();
            String currentEmailAddress = null;
            Set interests = new HashSet();

            while (rs.next()) {
                String thisEmailAddress = rs.getString("email");
                if (thisEmailAddress.equals(currentEmailAddress) || currentEmailAddress == null) {
                    interests.add(rs.getString("code"));
                    currentEmailAddress = thisEmailAddress;
                } else {
                    appendCustomObject(currentEmailAddress, interests, customObjects);
                    currentEmailAddress = thisEmailAddress;
                    interests.clear();
                    interests.add(rs.getString("code"));
                }
            }

            //append the last one
            appendCustomObject(currentEmailAddress, interests, customObjects);
            request.setCustomObjList(customObjects);
            rowsProcessed = customObjects.getCustomObjs().size();
            return request;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }
}
