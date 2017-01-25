package co.launchable.api.viator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * Created by MMcElligott on 10/28/2014.
 */
public class ConnectionTest {
    public String username;
    public String password;
    public String driver;
    public String url;
    public String sql;

    public void test() {
        try {
            Object o = Class.forName(driver).newInstance();
            Connection con = DriverManager.getConnection(url, username, password);
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                System.out.println("row loaded");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        ConnectionTest connectionTest = new ConnectionTest();
        connectionTest.username = args[0];
        connectionTest.password = args[1];
        connectionTest.driver = args[2];
        connectionTest.url = args[3];
        connectionTest.sql = args[4];
        connectionTest.test();
    }
}
