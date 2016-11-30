package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.springframework.core.env.Environment;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;

/**
 * Created by michaelmcelligott on 1/23/14.
 */
public class ApiSyncCustomVisitations extends ApiSyncObjectBase {
    private String prefixLocal = "marketo.syncVisitations.";
    private String objectTypeName = "Visitation";
    private String columnKeys;
    private String[] attributes;
    private String[] keys;

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

    public ApiSyncCustomVisitations(Environment env) {
        attributes = ((String)env.getProperty(getPrefixLocal() + "attributes")).split(",");
        keys = ((String)env.getProperty(getPrefixLocal() + "keys")).split(",");
        super.initialize(env);
    }

    public String getPrefixLocal() {
        return prefixLocal;
    }

    protected ParamsSyncCustomObjects buildRequest(ResultSet rs) {
        ParamsSyncCustomObjects request = new ParamsSyncCustomObjects();
        request.setObjTypeName(objectTypeName);
        JAXBElement<SyncOperationEnum> operation = new ObjectFactory().createParamsSyncCustomObjectsOperation(SyncOperationEnum.UPSERT);
        request.setOperation(operation);

        ArrayOfCustomObj customObjects = new ArrayOfCustomObj();

        try {
            while (rs.next()) {
                CustomObj customObj = new CustomObj();

                //set up key attributes
                ArrayOfAttribute arrayOfKeyAttributes = new ArrayOfAttribute();
                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i].trim();
                    Object value = rs.getObject(key);
                    arrayOfKeyAttributes.getAttributes().add(buildAttribute(key, value.toString()));
                }

                JAXBElement<ArrayOfAttribute> keyAttributes = new ObjectFactory().createCustomObjCustomObjKeyList(arrayOfKeyAttributes);
                customObj.setCustomObjKeyList(keyAttributes);

                ArrayOfAttribute arrayOfValueAttributes = new ArrayOfAttribute();
                for (int i = 0; i < attributes.length; i++) {
                    String key = attributes[i].trim();
                    Object value = rs.getObject(key);

                    if (!rs.wasNull()) {
                        arrayOfValueAttributes.getAttributes().add(buildAttribute(key, value.toString()));
                    }
                }

                JAXBElement<ArrayOfAttribute> valueAttributes = new ObjectFactory().createCustomObjCustomObjAttributeList(arrayOfValueAttributes);
                customObj.setCustomObjAttributeList(valueAttributes);
                customObjects.getCustomObjs().add(customObj);
            }

            request.setCustomObjList(customObjects);
            rowsProcessed = customObjects.getCustomObjs().size();
            return request;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }
}
