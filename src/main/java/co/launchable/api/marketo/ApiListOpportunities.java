package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Michael on 5/19/2015.
 */
public class ApiListOpportunities {
    @Autowired
        private MarketoApiConfig marketoApiConfig;

    public void execute() {
        // Create Request
        ParamsGetMObjects request = new ParamsGetMObjects();
        request.setType("Opportunity");
        ArrayOfMObjCriteria mObjCriteria= new ArrayOfMObjCriteria();
        request.setMObjCriteriaList(mObjCriteria);

        MObjCriteria criteria1 = new MObjCriteria();
        criteria1.setAttrName("Id");
        criteria1.setComparison(ComparisonEnum.GE);
        criteria1.setAttrValue("464000");
        mObjCriteria.getMObjCriterias().add(criteria1);

        String streamPosition = null;
        boolean hasMore = true;
//        MObjCriteria criteria2 = new MObjCriteria();
//        criteria2.setAttrName("Created At");
//        criteria2.setComparison(ComparisonEnum.GE);
//        criteria2.setAttrValue("2013-08-01T00:13:13+00:00");
//        mObjCriteria.getMObjCriterias().add(criteria2);

        while (hasMore) {
            if (streamPosition != null)
                request.setStreamPosition(streamPosition);

            AuthenticationHeader header = MarketoUtils.generateAuthenticationHeader(marketoApiConfig);
            SuccessGetMObjects result = marketoApiConfig.getMarketoWebServicePort().getMObjects(request, header);

            try {
                JAXBContext context = JAXBContext.newInstance(SuccessGetMObjects.class);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                m.marshal(result, System.out);
            } catch (Exception e) {
                e.printStackTrace();
            }

            streamPosition = result.getResult().getNewStreamPosition();
            hasMore = result.getResult().isHasMore();
        }
    }

    public static void main(String args[]) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("C://dev/Projects/ASViatorIntegration/src/main/webapp/WEB-INF/marketo-dispatcher-servlet.xml");
        ApiListOpportunities apiListOpportunities = (ApiListOpportunities)ctx.getBean("apiListOpportunities");
        //DataSource dataSource = (DataSource)ctx.getBean("dataSourceContacts");
        //MarketoApiConfig marketoApiConfig =(MarketoApiConfig)ctx.getBean("marketoApiConfig");
        //apiSyncUpdatedInterests.setDataSource(dataSource);
        apiListOpportunities.execute();
    }
}
