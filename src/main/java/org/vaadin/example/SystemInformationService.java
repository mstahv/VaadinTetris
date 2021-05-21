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
    int startHeapUsers;
    int lastSessionSizeGuestimate;
    int lastSessionSizeGuestimateUsers;

    ScheduledFuture<?> scheduledFuture;

    public SystemInformationService(ScheduledExecutorService executorService) {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        platformMXBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            memoryMXBean.gc();
            heapMemoryUsage = heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            if(startHeapMemoryUsage == null && numberOfPlayers > 5) {
                startHeapMemoryUsage = heapMemoryUsage;
                startHeapUsers = numberOfPlayers;
            }
            if(numberOfPlayers > 10 && numberOfPlayers > lastSessionSizeGuestimateUsers) {
                lastSessionSizeGuestimateUsers = numberOfPlayers;
                // (a + xb = total1) || * -1
                // (a + yb = total2)
                //-xb + yb = total2 - total1
                // b = (total2 -total1)/(y-x)
                lastSessionSizeGuestimate = (int) ((heapMemoryUsage.getUsed() - startHeapMemoryUsage.getUsed())/(numberOfPlayers - startHeapUsers));
            }
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

    public int getLastSessionSizeGuestimate() {
        return lastSessionSizeGuestimate;
    }

}
