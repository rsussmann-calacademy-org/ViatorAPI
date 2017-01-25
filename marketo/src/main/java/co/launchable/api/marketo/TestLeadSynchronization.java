package co.launchable.api.marketo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by McElligott on 8/20/2014.
 */
public class TestLeadSynchronization {
    public static void main(String[] args) {
        System.out.println("************** BEGINNING TestLeadSynchronization **************");

        ApplicationContext context = new FileSystemXmlApplicationContext("C:\\Projects\\ASViatorIntegration\\src\\main\\webapp\\WEB-INF\\mvc-dispatcher-servlet.xml");
        ServiceMarketo serviceMarketo = (ServiceMarketo)context.getBean("serviceMarketo");
        serviceMarketo.syncPendingLeads();

        System.out.println("************** ENDING TestLeadSynchronization *****************");
    }
}
