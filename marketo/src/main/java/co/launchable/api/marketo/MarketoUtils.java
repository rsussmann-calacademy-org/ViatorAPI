package co.launchable.api.marketo;

import com.marketo.mktows.AuthenticationHeader;
import com.marketo.mktows.MktMktowsApiService;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michael on 5/13/2015.
 */
public class MarketoUtils {

    public static AuthenticationHeader generateAuthenticationHeader(MarketoApiConfig marketoApiConfig) {
        try {
            URL marketoSoapEndPoint = new URL(marketoApiConfig.getMarketoEndpoint());

            QName serviceName = new QName(marketoApiConfig.getMarketoServiceQnameUrl(), marketoApiConfig.getMarketoServiceQnameName());
            MktMktowsApiService service = new MktMktowsApiService(marketoSoapEndPoint, serviceName);
            marketoApiConfig.setMarketoWebServicePort(service.getMktowsApiSoapPort());

            // Create Signature
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            String text = df.format(new Date());
            String requestTimestamp = text.substring(0, 22) + ":" + text.substring(22);
            String encryptString = requestTimestamp + marketoApiConfig.getMarketoUserId();

            SecretKeySpec secretKey = new SecretKeySpec(marketoApiConfig.getMarketoSecretKey().getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(encryptString.getBytes());
            char[] hexChars = Hex.encodeHex(rawHmac);
            String signature = new String(hexChars);

            // Set Authentication Header
            AuthenticationHeader header = new AuthenticationHeader();
            header.setMktowsUserId(marketoApiConfig.getMarketoUserId());
            header.setRequestTimestamp(requestTimestamp);
            header.setRequestSignature(signature);
            return header;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
