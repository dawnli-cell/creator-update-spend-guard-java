package com.example.creator;

public final class SpendGuardServiceTest {
    public static void main(String[] args) {
        SpendGuardService service = new SpendGuardService(null, 5.0);
        if (!service.canProcess(4.0, 1.0)) throw new AssertionError("exact cap should pass");
        if (service.canProcess(4.01, 1.0)) throw new AssertionError("over cap should reject");
        System.out.println("Spend decision test passed");
    }
}
