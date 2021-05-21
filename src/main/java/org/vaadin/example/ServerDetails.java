package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;

import java.lang.management.MemoryUsage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Matti Tahvonen
 */
public class ServerDetails extends Div {

    private final ScheduledFuture<?> scheduledFuture;
    private final SystemInformationService sis;
    private Paragraph memory = new Paragraph();
    private Paragraph cpu = new Paragraph();
    private Paragraph players = new Paragraph();


    public ServerDetails(SystemInformationService sis, ScheduledExecutorService es) {
        this.sis = sis;
        scheduledFuture = es.scheduleWithFixedDelay(this::updateDetails, 5, 5, TimeUnit.SECONDS);
        add(new H3("Server details"));
        add(new Paragraph("As a demo purpose, this is on purpose deployed on a moderate Amazon EC2 instance. Below some usage stats. TODO refine calculations"));
        add(memory,cpu, players);

        Paragraph latencyReport = new Paragraph();
        Button testLatency = new Button("Ping server (test server-round-trip latency)");
        testLatency.setId("latencyTestButton");
        // store the click time on the client side before hitting the server
        testLatency.getElement().executeJs("var el = this; this.addEventListener('click', function(){el.start = new Date().getTime();});");
        testLatency.addClickListener(e -> {
            // Ask the browser to execute this JS that reads the click time and reports how much it took to get back
            latencyReport.getElement().executeJs("var start = document.getElementById('latencyTestButton').start; this.innerHTML='Server roundtrip and client side overhead took: ' + (new Date().getTime() - start) +'ms';");
        });
        add(testLatency, latencyReport);

        doUpdateDetails();
    }

    private void doUpdateDetails() {
        MemoryUsage usage = sis.getHeapMemoryUsage();
        memory.setText(String.format("Heap used/available: %s/%s ",
                formatSize(usage.getUsed()),
                formatSize(usage.getMax()))
        );
        cpu.setText(String.format("CPU usage: %,.2f", sis.getSystemLoad()));

        // this estimation is twice too bad in the beginning and may change due to JVM optimisations, thus report only after n users are in
        String max;
        if(sis.getLastSessionSizeGuestimate() > 0) {
            max = "" + sis.getNumberOfPlayers() + (usage.getMax() - usage.getUsed())/sis.getLastSessionSizeGuestimate();
        } else {
            max = "unknown (more players needed for an estimate)";
        }
        players.setText("Active players on this node/ estimated max:" + sis.getNumberOfPlayers() + "/" + max + ", memory per player " + formatSize(sis.getLastSessionSizeGuestimate()));
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }

    private void updateDetails() {
        getUI().ifPresentOrElse(ui -> ui.access(()-> {
            doUpdateDetails();
        }), () -> {
            scheduledFuture.cancel(true);
        });
    }

}
