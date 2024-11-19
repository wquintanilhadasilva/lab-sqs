package com.lab.sqs.receiver;

import java.time.ZonedDateTime;

public record ReceiverResult(ZonedDateTime dateTime, String message) {
}
