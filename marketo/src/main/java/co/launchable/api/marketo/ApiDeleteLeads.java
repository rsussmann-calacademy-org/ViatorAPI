package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by mike on 4/5/16.
 */
public class ApiDeleteLeads implements Runnable {
    @Autowired
    private MarketoRestApi marketoRestApi;
    private long[] ids;

    public long[] getIds() {
        return ids;
    }

    public void setIds(long[] ids) {
        this.ids = ids;
    }

    public void run() {
        if (ids.length > 0) {
            synchronized (marketoRestApi) {
                marketoRestApi.refreshAccessToken();
                marketoRestApi.deleteLeads(ids);
            }
        }
    }
}
