package co.launchable.api.marketo;

import org.springframework.core.env.Environment;

/**
 * Created by Michael on 3/31/2015.
 */
public class ApiUpdateBadLeads extends ApiSyncMultipleLeads {
    private String prefixLocal = "marketo.updateBadLeads.";

    public ApiUpdateBadLeads(Environment env) {
        super(env);
        setSql(env.getProperty(prefixLocal + "sql"));
        setSqlBefore(env.getProperty(prefixLocal + "sqlBefore"));
        setSqlAfterSuccess(env.getProperty(prefixLocal + "sqlAfterSuccess"));
        setSqlAfterFailure(env.getProperty(prefixLocal + "sqlAfterFailure"));
        setSqlInsertStatus(env.getProperty(prefixLocal + "sqlInsertStatus"));
    }

    public String getSqlInsertStatus() {
        return null;
    }
}
