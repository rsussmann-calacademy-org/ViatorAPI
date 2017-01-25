package co.launchable.api.jobs;

import java.util.Date;
import java.util.List;

/**
 * Created by michaelmcelligott on 2/26/14.
 */
public interface JobStatusService {
    public String addWorkerList(List workers);
    public List getWorkersForUUID(String uuid);
    public Date getJobCreation(String uuid);
}
