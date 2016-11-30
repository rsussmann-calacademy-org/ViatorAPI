package co.launchable.api.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
class ContextClosedHandler implements ApplicationListener<ContextClosedEvent> {
    @Autowired
    ThreadPoolTaskExecutor executor;

    @Autowired
    ThreadPoolTaskScheduler scheduler;

    public void onApplicationEvent(ContextClosedEvent event) {
        scheduler.shutdown();
        executor.shutdown();
    }
}