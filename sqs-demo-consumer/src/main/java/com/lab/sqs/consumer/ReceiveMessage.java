package com.lab.sqs.consumer;

import java.time.ZonedDateTime;

public record ReceiveMessage(String id,
                             ZonedDateTime firstQueueDateTime,
                             ZonedDateTime lastQueueDateTime,
                             String textMessage,
                             Integer equeueCount) {
}
