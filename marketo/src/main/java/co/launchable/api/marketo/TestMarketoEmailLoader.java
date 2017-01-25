package co.launchable.api.marketo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Created by Michael on 3/31/2015.
 */
public class TestMarketoEmailLoader {
    public static void main(String[] args) {
        System.out.println("************** BEGINNING TestLeadSynchronization **************");

        ApplicationContext context = new FileSystemXmlApplicationContext("C:\\dev\\Projects\\ASViatorIntegration\\src\\main\\webapp\\WEB-INF\\co.launchable.api.marketo.marketo-dispatcher-servlet.xml");
        DataSource dataSource = (DataSource)context.getBean("dataSourceContacts");

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO MarketoEmails (email) VALUES (?)");

            BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\dev\\Projects\\ASViatorIntegration\\marketoEmails.txt")));
            String line = reader.readLine();
            int index = 0;
            while (line != null) {
                pstmt.setString(1, line);
                pstmt.execute();
                System.out.print(++index);
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("************** ENDING TestLeadSynchronization *****************");
    }
}
