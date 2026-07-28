package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class TCGameTestRegistration {
    private TCGameTestRegistration() {}

    public static void registerTests(RegisterGameTestsEvent event) {
        TCTestRegistrar r = new TCTestRegistrar(event);
        DataValidationTests.register(r);
        AuraTests.register(r);
        NodeTests.register(r);
        TransducerTests.register(r);
        RelayTests.register(r);
        TaintTests.register(r);
        EssentiaTests.register(r);
        ResearchTests.register(r);
        WandTests.register(r);
    }
}
