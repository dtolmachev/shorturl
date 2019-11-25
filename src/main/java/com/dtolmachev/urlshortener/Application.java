package com.dtolmachev.urlshortener;

import com.dtolmachev.urlshortener.service.Service;
import com.google.common.base.Stopwatch;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class Application {

    public static void main(String[] args) {
        try {
            AtomicBoolean cancelled = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);
            Service service = new Service();

            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> shutdown(service, latch, cancelled),
                    service.getName() + " Shutdown"
            ));
            start(service);
            latch.await();
        } catch (Throwable t) {
            log.error("Not started: {}", t.getMessage(), t);
            System.exit(1);
        }
    }

    public static void start(Service service) {
        Stopwatch sw = Stopwatch.createStarted();
        log.info("Starting {} service...", service.getName());
        service.start();
        log.info("Started successfully in {}", sw.stop());
    }

    public static void shutdown(Service service, CountDownLatch latch, AtomicBoolean cancelled) {
        if (cancelled.get()) {
            return;
        }
        cancelled.set(true);
        stop(service, latch);
    }

    public static void stop(Service service, CountDownLatch latch) {
        log.info("Stopping {} service...", service.getName());
        try {
            service.stop();
        } catch (Throwable t) {
            log.error("error during system stop", t);
            throw t;
        } finally {
            log.info("Service {} stopped", service.getName());
            latch.countDown();
        }
    }
}
