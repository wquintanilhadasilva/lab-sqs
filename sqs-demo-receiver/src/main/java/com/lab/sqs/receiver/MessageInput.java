package com.lab.sqs.receiver;

import java.time.ZonedDateTime;

public record MessageInput(String id,
                           ZonedDateTime firstQueueDateTime,
                           ZonedDateTime lastQueueDateTime,
                           String textMessage,
                           Integer equeueCount) {
}
