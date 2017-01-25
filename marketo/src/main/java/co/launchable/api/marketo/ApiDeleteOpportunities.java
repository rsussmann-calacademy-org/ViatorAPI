package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Created by Michael on 5/19/2015.
 */
public class ApiDeleteOpportunities {
    @Autowired
    private MarketoApiConfig marketoApiConfig;

    private int[] ids;
    private int start;
    private int end;
    private int maxItems = 100;

    public int[] getIds() {
        return ids;
    }

    public void setIds(int[] ids) {
        this.ids = ids;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void execute() {
        ArrayOfMObject objList = new ArrayOfMObject();
        for (int i = start; i <= end; i++) {

            MObject mobj = new MObject();
            mobj.setType("Opportunity");
            mobj.setId(i);
            objList.getMObjects().add(mobj);

            if (objList.getMObjects().size() >= maxItems || i == end) {
                ParamsDeleteMObjects request = new ParamsDeleteMObjects();
                request.setMObjectList(objList);
                AuthenticationHeader header = MarketoUtils.generateAuthenticationHeader(marketoApiConfig);
                SuccessDeleteMObjects result = marketoApiConfig.getMarketoWebServicePort().deleteMObjects(request, header);

                try {
                    JAXBContext context = JAXBContext.newInstance(SuccessDeleteMObjects.class);
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    m.marshal(result, System.out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                objList = new ArrayOfMObject();
            }
        }
    }


    public static void main(String args[]) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("C://dev/Projects/ASViatorIntegration/src/main/webapp/WEB-INF/co.launchable.api.marketo.marketo-dispatcher-servlet.xml");
        ApiDeleteOpportunities apiOperation = (ApiDeleteOpportunities)ctx.getBean("apiDeleteOpportunities");
        apiOperation.setStart(465811);
        apiOperation.setEnd(471010);
        //apiOperation.setIds(new int[] {478888});
        apiOperation.execute();
    }
}
