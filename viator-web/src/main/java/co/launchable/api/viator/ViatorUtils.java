package co.launchable.api.viator;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by Michael on 4/29/2015.
 */
public class ViatorUtils {

    public static String getAgeBandSummaryString(Map<String, Integer> ageBandCounts) {
        StringBuffer sb = new StringBuffer();
        String[] ageBandTypes = new String[] {"ADULT", "YOUTH", "CHILD", "SENIOR", "INFANT"};

        for (int i = 0; i < ageBandTypes.length; i++) {
            String ageBandType = ageBandTypes[i];
            Integer count = ageBandCounts.get(ageBandType);
            if (count != null) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(count);
                sb.append(" ");
                sb.append(StringUtils.capitalize(ageBandType.toLowerCase()));
            }
        }
        return sb.toString();
    }

    public static void addToAgeBand(String ageBand, Map ageBandCounts) {
        Integer currentCount =(Integer)ageBandCounts.get(ageBand);
        if (currentCount == null) {
            ageBandCounts.put(ageBand, 1);
        } else {
            currentCount = currentCount + 1;
            ageBandCounts.put(ageBand, currentCount);
        }
    }
}
