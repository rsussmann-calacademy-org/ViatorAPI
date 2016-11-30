package co.launchable.api;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Created by michaelmcelligott on 6/10/14.
 */
public class SpringLoadedTest {
    public static void main(String[] args) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("//Users/michaelmcelligott/Dropbox/Projects/ASViatorIntegration/src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml");
        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) ctx.getBean("myScheduler");
    }
}
