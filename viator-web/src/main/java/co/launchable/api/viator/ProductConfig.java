package co.launchable.api.viator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by McElligott on 6/27/2014.
 */
public class ProductConfig {
    String productCode;
    String productName;
    String visualIdPrefix;
    Boolean ageBandInsensitive = Boolean.FALSE;
    Map ageBandsToVisualIdPrefixes = new HashMap();
    Map ageBandsToConfigMaps = new HashMap();

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVisualIdPrefix() {
        return visualIdPrefix;
    }

    public String getIdBucket(String ageBand) {
        if (ageBandInsensitive)
            return productCode;
        else
            return productCode + "-" + ageBand;
    }

    public List getAllIdBuckets() {
        List buckets = new ArrayList();
        if (ageBandInsensitive)
            buckets.add(productCode);
        else {
            buckets.add(productCode + "-Adult");
            buckets.add(productCode + "-Child");
            buckets.add(productCode + "-Youth");
            buckets.add(productCode + "-Senior");
            buckets.add(productCode + "-Student");
        }
        return buckets;
    }

    public void setVisualIdPrefix(String visualIdPrefix) {
        this.visualIdPrefix = visualIdPrefix;
    }

    public Boolean getAgeBandInsensitive() {
        return ageBandInsensitive;
    }

    public void setAgeBandInsensitive(Boolean ageBandInsensitive) {
        this.ageBandInsensitive = ageBandInsensitive;
    }

    public Map getAgeBandsToVisualIdPrefixes() {
        return ageBandsToVisualIdPrefixes;
    }

    public void setAgeBandsToVisualIdPrefixes(Map ageBandsToVisualIdPrefixes) {
        this.ageBandsToVisualIdPrefixes = ageBandsToVisualIdPrefixes;
    }

    public Map getAgeBandsToConfigMaps() {
        return ageBandsToConfigMaps;
    }

    public void setAgeBandsToConfigMaps(Map ageBandsToConfigMaps) {
        this.ageBandsToConfigMaps = ageBandsToConfigMaps;
    }

    public String getVisualIdPrefix(String ageBand) {
        if (ageBandInsensitive)
            return visualIdPrefix;
        else {
            return (String)ageBandsToVisualIdPrefixes.get(ageBand);
        }
    }
}
