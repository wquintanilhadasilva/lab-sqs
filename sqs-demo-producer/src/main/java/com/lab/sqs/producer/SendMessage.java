package com.lab.sqs.producer;

import java.time.ZonedDateTime;

public record SendMessage(String id,
                          ZonedDateTime firstQueueDateTime,
                          ZonedDateTime lastQueueDateTime,
                          String textMessage,
                          Integer equeueCount) {
}
