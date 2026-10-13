package com.computerwhz;

import com.computerwhz.calagopus.model.ServerLimits;
import com.computerwhz.calagopus.JSON;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServerLimitsCompatibilityTest {
    @Test public void acceptsPanelBandwidthWithoutLosingResourceLimits() throws Exception {
        new CalagopusManager("https://panel.example.com", "test-key");
        String payload = "{\"cpu\":0,\"memory\":4096,\"swap\":0,\"disk\":0,"
                + "\"bandwidth\":{\"upload\":0,\"download\":0}}";
        // Parent server validators call this directly before deserialization.
        ServerLimits.validateJsonElement(JsonParser.parseString(payload));
        ServerLimits limits = JSON.deserialize(payload, ServerLimits.class);
        assertEquals(4096L, limits.getMemory().longValue());
        assertEquals(0L, limits.getDisk().longValue());
        assertEquals(0L, limits.getCpu().longValue());
    }

    @Test public void stillAcceptsOlderPanelsWithoutBandwidth() throws Exception {
        new CalagopusManager("https://panel.example.com", "test-key");
        ServerLimits limits = JSON.deserialize(
                "{\"cpu\":100,\"memory\":2048,\"swap\":0,\"disk\":8192}", ServerLimits.class);
        assertEquals(2048L, limits.getMemory().longValue());
    }

    @Test public void malformedKnownLimitsAreStillRejected() {
        new CalagopusManager("https://panel.example.com", "test-key");
        try {
            JSON.deserialize("{\"cpu\":0,\"memory\":{},\"swap\":0,\"disk\":0,"
                    + "\"bandwidth\":{\"upload\":0,\"download\":0}}", ServerLimits.class);
            fail("Malformed memory must not be accepted");
        } catch (RuntimeException expected) {
            // Existing type validation remains active.
        }
    }
}
