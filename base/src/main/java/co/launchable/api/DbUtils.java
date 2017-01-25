package co.launchable.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by McElligott on 8/19/2014.
 */
public class DbUtils {

    public static void loadObjectsFromDatabase(Connection con, String query, String keyProperty, Map resultMap, List resultList) throws SQLException {

        ResultSet rs = con
                .createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                .executeQuery(query);

        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        while (rs.next()) {
            Map<String, Object> objectMap = new HashMap<String, Object>();

            for (int i = 1; i <= cols; i++) {
                objectMap.put(rsmd.getColumnLabel(i), rs.getObject(i));
            }
            if (resultMap != null)
                resultMap.put((String)objectMap.get(keyProperty), objectMap);

            if (resultList != null)
                resultList.add(objectMap);
        }
    }
}
