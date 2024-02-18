package com.ngrig.datatransfer.handler;

import java.time.Duration;

public interface Handler {
    Duration timeout();

    void performOperation();
}
