package co.launchable.api.paymentech;

import com.paymentech.orbital.sdk.interfaces.RequestIF;
import com.paymentech.orbital.sdk.interfaces.ResponseIF;


/**
 * Created with IntelliJ IDEA.
 * User: michaelmcelligott
 * Date: 6/20/13
 * Time: 9:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class Exchange {
    private RequestIF request;
    private ResponseIF response;

    public RequestIF getRequest() {
        return request;
    }

    public void setRequest(RequestIF request) {
        this.request = request;
    }

    public ResponseIF getResponse() {
        return response;
    }

    public void setResponse(ResponseIF response) {
        this.response = response;
    }

    public void printResponseCodes() {
        String separationCharacter = ",";
        String orderId = getRequest().getField("OrderID");
        String trace = getRequest().getTraceNumber();

        if (response != null) {
            //System.out.print(orderId + ",");
            System.out.print(orderId + "[" + trace + "]" + separationCharacter);
            System.out.println(response.getValue("AuthCode") + separationCharacter  + response.getValue("RespCode") + separationCharacter + response.getValue("AVSRespCode") + separationCharacter + response.getCVV2RespCode() + separationCharacter +  response.getValue("TxRefNum"));
        } else {
            System.out.println(orderId + ": No response.");
        }
    }
}
