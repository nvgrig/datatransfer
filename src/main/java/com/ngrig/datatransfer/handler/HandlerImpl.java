package com.ngrig.datatransfer.handler;

import com.ngrig.datatransfer.Model.Address;
import com.ngrig.datatransfer.Model.Event;
import com.ngrig.datatransfer.Model.Payload;
import com.ngrig.datatransfer.Model.Result;
import com.ngrig.datatransfer.client.Client;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class HandlerImpl implements Handler {

    private static final long TIME_OUT_SECONDS = 3;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Client client;

    public HandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public Duration timeout() {
        return Duration.ofSeconds(TIME_OUT_SECONDS);
    }

    @Override
    public void performOperation() {
        final Event event = client.readData();
        List<Address> rejectedList = sendData(event.recipients(), event.payload(), false);
        while (!rejectedList.isEmpty()) {
            rejectedList = sendData(rejectedList, event.payload(), true);
        }
    }

    private List<Address> sendData(List<Address> recipients, Payload payload, boolean repeat) {

        if (repeat) {
            try {
                Thread.sleep(timeout().toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Map<Address, Future<Result>> futureMap = recipients.stream()
                .collect(Collectors.toMap(address -> address, address -> executor.submit(() -> client.sendData(address, payload))));

        List<Address> rejectedList = new ArrayList<>();
        while (!futureMap.isEmpty()) {
            recipients.forEach(address -> {
                Future<Result> resultFuture = futureMap.get(address);
                if (resultFuture.isDone()) {
                    try {
                        Result result = resultFuture.get();
                        if (result.equals(Result.ACCEPTED)) {
                            futureMap.remove(address);
                        }
                        if (result.equals(Result.REJECTED)) {
                            rejectedList.add(address);
                            futureMap.remove(address);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }

        return rejectedList;
    }
}
