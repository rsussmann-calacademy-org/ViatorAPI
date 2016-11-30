package co.launchable.api.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceClassPathResolver;
import org.apache.commons.mail.resolver.DataSourceUrlResolver;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.apache.commons.mail.HtmlEmail;

import java.io.StringWriter;
import java.net.URL;

/**
 * Created by McElligott on 8/19/2014.
 */
@PropertySource("email.properties")
@Service
public class ServiceEmail implements InitializingBean {
    @Autowired
    Environment env;

    String username;
    String password;
    String fromAddress;
    String fromName;
    String host;        //"smtp.googlemail.com"
    String smtpPort;    //"465"

    @Override
    public void afterPropertiesSet() throws Exception {
        username = env.getProperty("emailUsername");
        password = env.getProperty("emailPassword");
        fromAddress = env.getProperty("emailFromAddress");
        fromName = env.getProperty("emailFromName");
        host = env.getProperty("emailHost");
        smtpPort = env.getProperty("emailSmtpPort");
    }

    public void sendHtmlEmail(String subject, String[] recipients, String message) {

        try {
            // create the email message
            ImageHtmlEmail email = new ImageHtmlEmail();
            email.setSmtpPort(Integer.parseInt(smtpPort));
            email.setAuthenticator(new DefaultAuthenticator(username, password));
            email.setSSLOnConnect(true);
            email.setSubject(subject);
            email.setDataSourceResolver(new DataSourceClassPathResolver());
            email.setHostName(host);
            email.setFrom(fromAddress, fromName);
            email.setSubject(subject);

            for (int i = 0; i < recipients.length; i++) {
                String recipient = recipients[i];
                String[] parts = recipient.split(";");
                if (parts.length == 2)
                    email.addTo(parts[0], parts[1]);
                else if (parts.length == 1)
                    email.addTo(parts[0]);
            }

            email.setHtmlMsg(message);
            email.setTextMsg("Your email client does not support HTML messages");
            email.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
