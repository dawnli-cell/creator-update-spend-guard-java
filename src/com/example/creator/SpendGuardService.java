package com.example.creator;

import java.io.IOException;

public final class SpendGuardService {
    private final InfraiClient infrai;
    private final double hardCapUsd;

    public SpendGuardService(InfraiClient infrai, double hardCapUsd) {
        this.infrai = infrai;
        this.hardCapUsd = hardCapUsd;
    }

    public boolean canProcess(double observedSpendUsd, double estimateUsd) {
        return observedSpendUsd + estimateUsd <= hardCapUsd;
    }

    public String processSubscriberUpdate(String text, double observedSpendUsd, double estimateUsd)
            throws IOException, InterruptedException {
        if (!canProcess(observedSpendUsd, estimateUsd)) {
            return "REJECTED_BUDGET";
        }
        String payload = "{\"model\":\"auto\",\"messages\":[{\"role\":\"user\",\"content\":\"" + escape(text) + "\"}]}";
        return infrai.request("POST", "/v1/chat/completions", payload);
    }

    public void configureBudget() throws IOException, InterruptedException {
        String payload = "{\"hard_cap_usd\":" + hardCapUsd + ",\"period\":\"monthly\",\"alert_threshold_usd\":" + (hardCapUsd * 0.8) + "}";
        infrai.request("PUT", "/v1/account/budget/set", payload);
    }

    public String usageTimeseries() throws IOException, InterruptedException {
        return infrai.request("GET", "/v1/account/usage/timeseries", null);
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
}
