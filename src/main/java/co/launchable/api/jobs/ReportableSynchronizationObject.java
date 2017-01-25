package co.launchable.api.jobs;

/**
 * Created by Michael on 12/31/2014.
 */
public interface ReportableSynchronizationObject {
    public int getFullRowsProcessed();
    public int getRowsToProcess();

}
