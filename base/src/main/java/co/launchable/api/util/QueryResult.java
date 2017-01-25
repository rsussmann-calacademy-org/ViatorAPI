package co.launchable.api.util;

import java.util.List;

/**
 * Created by mike on 1/15/16.
 */
public class QueryResult {
    public String title;
    public List results;

    public QueryResult(String title, List results) {
        this.title = title;
        this.results = results;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List getResults() {
        return results;
    }

    public void setResults(List results) {
        this.results = results;
    }
}
