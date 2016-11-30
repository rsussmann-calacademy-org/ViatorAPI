package co.launchable.api.viator;

import co.launchable.api.marketo.ServiceMarketo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michael on 3/5/2015.
 */
public class TestTimeoutEmail {
    public static void main(String[] args) {
        System.out.println("************** BEGINNING TestTimeoutEmail **************");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ApplicationContext context = new FileSystemXmlApplicationContext("C:\\dev\\projects\\ASViatorIntegration\\src\\main\\webapp\\WEB-INF\\viator-dispatcher-servlet.xml");
        ControllerViator controllerViator = (ControllerViator)context.getBean("controllerViator");

        try {
            Date start = sdf.parse("2014-01-01 12:10:00");
            Date end = sdf.parse("2014-01-01 12:10:10");
            controllerViator.sendTimeoutEmail(start, end, "TestSeconds10");

            start = sdf.parse("2014-01-01 12:10:00");
            end = sdf.parse("2014-01-01 12:10:32");
            controllerViator.sendTimeoutEmail(start, end, "TestSeconds32");

        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        System.out.println("************** ENDING TestTimeoutEmail *****************");
    }
}
