package co.launchable.api.viator;

import co.launchable.api.email.ServiceEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.env.Environment;

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Michael on 3/5/2015.
 */
public class RunSendTimeoutEmail implements Runnable {
    Date start;
    Date end;
    long emailTransactionTimeout;
    String transaction;
    Environment env;
    ServiceEmail serviceEmail;
    DateFormat dateFormat;
    VelocityEngine velocityEngine;
    String emailReportRecipients;

    public RunSendTimeoutEmail(Date start, Date end, String transaction, long emailTransactionTimeout, String emailReportRecipients, ServiceEmail serviceEmail, DateFormat dateFormat, VelocityEngine velocityEngine) {
        this.start = start;
        this.end = end;
        this.emailTransactionTimeout = emailTransactionTimeout;
        this.emailReportRecipients = emailReportRecipients;
        this.serviceEmail = serviceEmail;
        this.dateFormat = dateFormat;
        this.velocityEngine = velocityEngine;
        this.transaction = transaction;
    }

    @Override
    public void run() {
        long msStart = start.getTime();
        long msEnd = end.getTime();
        long ms = msEnd - msStart;
        int seconds = (int)(ms/1000);

        VelocityContext vc = new VelocityContext();

        String subject = "Viator transaction took " + seconds + " seconds";
        vc.put("ms", ms);
        vc.put("seconds", seconds);
        vc.put("timeStarted", dateFormat.format(start));
        vc.put("timeEnded", dateFormat.format(end));
        vc.put("subject", subject);
        vc.put("endpoint", transaction);

        StringWriter stringWriter = new StringWriter();
        Template t = velocityEngine.getTemplate("templates/viator/tpl_viatorTimeout.vtl");
        t.merge(vc, stringWriter);

        String[] arrayRecipients = emailReportRecipients.split(",");
        serviceEmail.sendHtmlEmail(subject, arrayRecipients, stringWriter.toString());
    }
}
