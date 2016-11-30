package co.launchable.api.viator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michael on 4/29/2015.
 */
public class TestAgeBandSummaryGeneration {

    public static void main(String args[]) {
        Map ageBandsToCounts = new HashMap();
        ViatorUtils.addToAgeBand("ADULT", ageBandsToCounts);
        ViatorUtils.addToAgeBand("ADULT", ageBandsToCounts);
        ViatorUtils.addToAgeBand("ADULT", ageBandsToCounts);
        ViatorUtils.addToAgeBand("YOUTH", ageBandsToCounts);
        ViatorUtils.addToAgeBand("YOUTH", ageBandsToCounts);

        String summary = ViatorUtils.getAgeBandSummaryString(ageBandsToCounts);
        System.out.println(summary);
    }
}
