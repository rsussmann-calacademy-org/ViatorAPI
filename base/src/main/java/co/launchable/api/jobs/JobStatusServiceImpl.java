package co.launchable.api.jobs;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by michaelmcelligott on 2/26/14.
 */
@Service
public class JobStatusServiceImpl implements JobStatusService {
    private Map uuidsToJobs = new HashMap();
    private Map uuidsToCreation = new HashMap();

    @Override
    public String addWorkerList(List workers) {
        String uuid = UUID.randomUUID().toString();
        uuidsToJobs.put(uuid, workers);
        uuidsToCreation.put(uuid, new Date());
        return uuid;
    }

    public List getWorkersForUUID(String uuid) {
        return (List)uuidsToJobs.get(uuid);
    }

    public Date getJobCreation(String uuid) {
        return (Date)uuidsToCreation.get(uuid);
    }
}
