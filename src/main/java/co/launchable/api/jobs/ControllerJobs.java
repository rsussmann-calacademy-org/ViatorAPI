package co.launchable.api.jobs;

import co.launchable.api.marketo.ReportableSynchronizationObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael on 7/23/2015.
 */
@Controller
@RequestMapping("/jobs")
public class ControllerJobs {
    @Autowired
    JobStatusService jobStatusService;

    @RequestMapping(value="/status", method= RequestMethod.GET)
    public ModelMap jobStatus(HttpServletRequest request) {
        ModelMap modelMap = new ModelMap();
        if (request.getParameter("uuid") != null) {
            int fullRowsProcessed = 0;
            int rowsToProcess = 10000000;
            List workers = jobStatusService.getWorkersForUUID(request.getParameter("uuid"));
            Date jobCreation = jobStatusService.getJobCreation(request.getParameter("uuid"));
            long secondsElapsed = (new Date().getTime()) - jobCreation.getTime();
            secondsElapsed = secondsElapsed / 1000;

            for (int i = 0; i < workers.size(); i++) {
                ReportableSynchronizationObject apiSyncObjectBase = (ReportableSynchronizationObject) workers.get(i);
                fullRowsProcessed += apiSyncObjectBase.getFullRowsProcessed();
                rowsToProcess = apiSyncObjectBase.getRowsToProcess() < rowsToProcess ? apiSyncObjectBase.getRowsToProcess() : rowsToProcess;
            }

            int percentageComplete = 0;
            if ((fullRowsProcessed + rowsToProcess) > 0)
                percentageComplete = (int)((100 * fullRowsProcessed) / (fullRowsProcessed + rowsToProcess));

            float rowsPerSecond = (float)fullRowsProcessed / (float)secondsElapsed;
            int secondsToGo = (int) ((float)rowsToProcess/(float)rowsPerSecond);

            modelMap.addAttribute("secondsElapsed", secondsElapsed);
            modelMap.addAttribute("secondsToGo", secondsToGo);
            modelMap.addAttribute("rowsProcessed", fullRowsProcessed);
            modelMap.addAttribute("rowsLeft", rowsToProcess);
            modelMap.addAttribute("percentageComplete", percentageComplete);
            modelMap.addAttribute("workers", workers);
            modelMap.addAttribute("uuid", request.getParameter("uuid"));
        }
        return modelMap;
    }
}
