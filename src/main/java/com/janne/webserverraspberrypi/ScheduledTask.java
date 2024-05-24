package com.janne.webserverraspberrypi;

import com.janne.webserverraspberrypi.services.FileScannerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {
    private final FileScannerService fileScannerService;

    public ScheduledTask(FileScannerService fileScannerService) {
        this.fileScannerService = fileScannerService;
    }


    @Scheduled(fixedRate = 100)
    public void myScheduledMethod() {
        fileScannerService.runScan();
    }
}
