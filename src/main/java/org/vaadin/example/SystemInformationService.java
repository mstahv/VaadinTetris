package org.vaadin.example;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SystemInformationService {

    private final OperatingSystemMXBean platformMXBean;
    private final MemoryMXBean memoryMXBean;
    private int numberOfPlayers = 0;
    MemoryUsage heapMemoryUsage;
    MemoryUsage startHeapMemoryUsage;
    ScheduledFuture<?> scheduledFuture;

    public SystemInformationService(ScheduledExecutorService executorService) {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        platformMXBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        executorService.schedule(() -> {
            memoryMXBean.gc();
            startHeapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        }, 3, TimeUnit.SECONDS);
        scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            memoryMXBean.gc();
            heapMemoryUsage = heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        }, 4, 20, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destroy() {
        scheduledFuture.cancel(true);
    }

    public void register() {
        numberOfPlayers++;
    }

    public void deRegister() {
        numberOfPlayers--;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public double getSystemLoad() {
        return platformMXBean.getSystemLoadAverage();
    }

    public MemoryUsage getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public MemoryUsage getStartHeapMemoryUsage() {
        return startHeapMemoryUsage;
    }
}
