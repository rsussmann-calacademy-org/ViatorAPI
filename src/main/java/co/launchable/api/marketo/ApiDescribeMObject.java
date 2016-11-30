package co.launchable.api.marketo;

import com.marketo.mktows.ParamsDescribeMObject;
import com.marketo.mktows.SuccessDescribeMObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.marketo.mktows.*;
import java.net.URL;
import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.springframework.core.env.Environment;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Created by michaelmcelligott on 2/4/14.
 */
public class ApiDescribeMObject extends ApiBase {
    private String objectType;

    public void initialize(Environment env) {
        super.initialize(env);
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public void execute() {
        try {
            // Set Authentication Header
            AuthenticationHeader header = generateAuthenticationHeader();

            // Create Request
            ParamsDescribeMObject request = new ParamsDescribeMObject();
            request.setObjectName(objectType);

            SuccessDescribeMObject result = getMarketoWebServicePort().describeMObject(request, header);
            JAXBContext context = JAXBContext.newInstance(SuccessDescribeMObject.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(result, System.out);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
