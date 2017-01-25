package co.launchable.api.marketo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by Michael on 3/20/2015.
 */
public class TestUpdatedLeadCalculation {
    public static void main(String[] args) {
        System.out.println("************** BEGINNING TestUpdatedLeadCalculation **************");

        ApplicationContext context = new FileSystemXmlApplicationContext("C:\\dev\\Projects\\ASViatorIntegration\\src\\main\\webapp\\WEB-INF\\co.launchable.api.marketo.marketo-dispatcher-servlet.xml");
        ServiceMarketo serviceMarketo = (ServiceMarketo)context.getBean("serviceMarketo");
        serviceMarketo.calculateUpdatedLeads();
        System.out.println("************** ENDING TestUpdatedLeadCalculation *****************");
    }
}
