package co.launchable.api.email;

import co.launchable.api.marketo.ServiceMarketo;
import co.launchable.api.viator.Booking;
import co.launchable.api.viator.BookingAction;
import co.launchable.api.viator.ServiceBooking;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.List;

/**
 * Created by McElligott on 8/19/2014.
 */
public class TestEmailService {
    public static void main(String[] args) {
        System.out.println("************** BEGINNING TestEmailService **************");

        ApplicationContext context = new FileSystemXmlApplicationContext("C:\\Projects\\ASViatorIntegration\\src\\main\\webapp\\WEB-INF\\mvc-dispatcher-servlet.xml");
        ServiceMarketo serviceMarketo = (ServiceMarketo)context.getBean("serviceMarketo");
        serviceMarketo.sendEmailReport();

        System.out.println("************** ENDING TestEmailService *****************");
    }
}
