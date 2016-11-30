package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.springframework.core.env.Environment;

import javax.xml.bind.JAXBElement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by michaelmcelligott on 2/6/14.
 */
public class ApiSyncCustomObjectsKeysAndAttributes extends ApiSyncObjectBase {
    private String prefixLocal;
    private String objectTypeName;
    private String[] keys;
    private String[] attributes;


    public String getObjectTypeName() {
        return objectTypeName;
    }

    public void setObjectTypeName(String objectTypeName) {
        this.objectTypeName = objectTypeName;
    }

    public ApiSyncCustomObjectsKeysAndAttributes(String prefixLocal, String objectTypeName, Environment env) {
        this.prefixLocal = prefixLocal;
        this.objectTypeName = objectTypeName;
        attributes = ((String)env.getProperty(getPrefixLocal() + "attributes")).split(",");
        keys = ((String)env.getProperty(getPrefixLocal() + "keys")).split(",");
        super.initialize(env);

        //override some properties if they exist
        if (env.containsProperty(getPrefixLocal()  + "marketoEndpoint"))
            setMarketoEndpoint(env.getProperty(getPrefixLocal()  + "marketoEndpoint"));
        if (env.containsProperty(getPrefixLocal()  + "marketoUserId"))
            setMarketoUserId(env.getProperty(getPrefixLocal()  + "marketoUserId"));
        if (env.containsProperty(getPrefixLocal()  + "marketoSecretKey"))
            setMarketoSecretKey(env.getProperty(getPrefixLocal()  + "marketoSecretKey"));
        if (env.containsProperty(getPrefixLocal()  + "marketoQnameUrl"))
            setMarketoServiceQnameUrl(env.getProperty(getPrefixLocal()  + "marketoQnameUrl"));
        if (env.containsProperty(getPrefixLocal()  + "marketoQnameName"))
            setMarketoServiceQnameName(env.getProperty(getPrefixLocal()  + "marketoQnameName"));
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
                    arrayOfKeyAttributes.getAttributes().add(buildAttribute(keys[i].trim(), rs.getObject(keys[i].trim())));
                }

                JAXBElement<ArrayOfAttribute> keyAttributes = new ObjectFactory().createCustomObjCustomObjKeyList(arrayOfKeyAttributes);
                customObj.setCustomObjKeyList(keyAttributes);

                ArrayOfAttribute arrayOfValueAttributes = new ArrayOfAttribute();
                for (int i = 0; i < attributes.length; i++) {
                    String key = attributes[i].trim();
                    if (!"".equals(key)) {
                        Object value = rs.getObject(key);

                        if (!rs.wasNull())
                            arrayOfValueAttributes.getAttributes().add(buildAttribute(key, value));
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
            setLastException(sqle);
        }
        return null;
    }
}
