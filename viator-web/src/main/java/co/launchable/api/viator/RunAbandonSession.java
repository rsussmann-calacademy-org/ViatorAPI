package co.launchable.api.viator;

import co.launchable.api.egalaxy.ServiceGalaxy;

/**
 * Created by mike on 2/22/16.
 */
public class RunAbandonSession implements Runnable{
    public ServiceGalaxy serviceGalaxy;
    public String sessionId;

    public ServiceGalaxy getServiceGalaxy() {
        return serviceGalaxy;
    }

    public void setServiceGalaxy(ServiceGalaxy serviceGalaxy) {
        this.serviceGalaxy = serviceGalaxy;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void run() {
        if (serviceGalaxy != null && sessionId != null)
            serviceGalaxy.apiAbandonSession(sessionId);
    }
}
