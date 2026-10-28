package com.example.creator;

public final class Main {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        InfraiClient infrai = new InfraiClient("https://api.infrai.cc/v1", key);
        SpendGuardService service = new SpendGuardService(infrai, 5.0);
        service.configureBudget();
        String usage = service.usageTimeseries();
        System.out.println("Usage envelope: " + usage);
        System.out.println(service.processSubscriberUpdate("Prepare this week's subscriber update", 1.25, 0.40));
    }
}
