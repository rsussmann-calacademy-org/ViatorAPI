package co.launchable.api.paymentech;

import com.paymentech.orbital.sdk.engine.EngineIF;
import com.paymentech.orbital.sdk.interfaces.RequestIF;
import com.paymentech.orbital.sdk.interfaces.ResponseIF;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: michaelmcelligott
 * Date: 6/19/13
 * Time: 10:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentechExecutor implements
        Runnable {
    Logger log = Logger.getLogger(PaymentechExecutor.class);

    private RequestIF request;
    private ResponseIF response;
    private EngineIF engine;
    private boolean running = true;
    private boolean shouldExit = false;

    public void cancel() {
        this.shouldExit = true;
    }

    public ResponseIF getResponse() {
        return response;
    }

    public void setResponse(ResponseIF response) {
        this.response = response;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public RequestIF getRequest() {
        return request;
    }

    public void setRequest(RequestIF request) {
        this.request = request;
    }

    public EngineIF getEngine() {
        return engine;
    }

    public void setEngine(EngineIF engine) {
        this.engine = engine;
    }

    public void run() {
        running = true;
        if (engine != null & request != null) {

            try {
                response = engine.execute(request);
                running = false;
            } catch (Exception e) {
                log.info("Exception posting transaction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
