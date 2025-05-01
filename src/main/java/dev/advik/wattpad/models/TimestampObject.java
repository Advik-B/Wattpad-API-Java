package dev.advik.wattpad.models;

import java.time.LocalDateTime;

public class TimestampObject {
    private final String timestamp;

    public TimestampObject(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAsString() {
        return timestamp;
    }

    public LocalDateTime getAsLocalDateTime() {
        /*
        datetime.strptime(json["createDate"], "%Y-%m-%dT%H:%M:%SZ")
         */

        // Assuming the timestamp is in the format "YYYY-MM-DDTHH:MM:SSZ"
        String formattedTimestamp = timestamp.replace("Z", "");
        return LocalDateTime.parse(formattedTimestamp);
    }
}
