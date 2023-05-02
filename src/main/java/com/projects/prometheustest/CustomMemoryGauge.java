package com.projects.prometheustest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class CustomMemoryGauge {

    private static final String totalMemoryWindows = "wmic ComputerSystem get TotalPhysicalMemory";

    private static final String freeMemoryWindows = "wmic OS get FreePhysicalMemory";

    private static final String[] linuxCommand = {"/bin/sh", "-c", "free | grep Mem:" };

    private final boolean systemIsWindows;

    private List<Long> linuxStats;

    public CustomMemoryGauge(MeterRegistry meterRegistry) {
        systemIsWindows = osIsWindows();

        if (!systemIsWindows) {

            linuxStats = getLinuxStats();

            Gauge
                    .builder("Used_Memory_Gauge",
                            () -> linuxStats.get(1))
                    .description("Currently used memory (MB)")
                    .register(meterRegistry);
            Gauge
                    .builder(
                            "Shared_Memory_Gauge",
                            () -> linuxStats.get(3))
                    .description("Total shared memory (MB)")
                    .register(meterRegistry);
            Gauge
                    .builder(
                            "Buffer/Cache_Memory_Gauge",
                            () -> linuxStats.get(4))
                    .description("Buffer/Cache Memory Usage (MB)")
                    .register(meterRegistry);
            Gauge
                    .builder(
                            "Available_Memory_Gauge",
                            () -> linuxStats.get(5))
                    .description("Available memory (MB)")
                    .register(meterRegistry);
        }

        Gauge
                .builder(
                        "Total_Memory_Gauge",
                        this::getTotalMemory)
                .description("Total Memory (MB)")
                .register(meterRegistry);
        Gauge
                .builder(
                        "Free_Memory_Gauge",
                        this::getFreeMemory)
                .description("Free memory (MB)")
                .register(meterRegistry);
    }

    private boolean osIsWindows() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return os.startsWith("windows");
    }

    private double getTotalMemory() {
        double totalMemory = 0.0;
        if (systemIsWindows) {
            totalMemory =
                    Double.parseDouble(String.valueOf(runWindowsMemoryCommand(totalMemoryWindows) / (1024 * 1024)));
        } else {
            totalMemory = Double.parseDouble(String.valueOf(linuxStats.get(0)));
        }
        return totalMemory;
    }

    private double getFreeMemory() {
        double freeMemory = 0;
        if (systemIsWindows) {
            freeMemory = Double.parseDouble(String.valueOf(runWindowsMemoryCommand(freeMemoryWindows) / (1024)));
        } else {
            List<Long> memoryList = getLinuxStats();
            freeMemory = Double.parseDouble(String.valueOf(linuxStats.get(2)));
        }
        return freeMemory;
    }

    private List<Long> getLinuxStats() {
        var memoryList = new ArrayList<Long>();
        try {
            var process = Runtime
                    .getRuntime()
                    .exec(linuxCommand);
            var bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            var line = bufferedReader.readLine();
            if (line != null && line.length() > 0) {
                var memoryValues = line.split("\\s+");
                for (int i = 1; i < memoryValues.length; i++) {
                    memoryList.add(Long.parseLong(memoryValues[i]));
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return memoryList;
    }

    private long runWindowsMemoryCommand(String command) {
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec(command);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int curLine = 0;
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() > 0) {
                    if (curLine == 1) {
                        return (Long.parseLong(line.strip()));
                    }
                    curLine++;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }

        return 0;
    }
}
