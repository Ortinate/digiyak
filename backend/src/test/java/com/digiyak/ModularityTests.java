package com.digiyak;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Enforces strict Spring Modulith boundaries across all 15-member modules in CI.
 * Fails immediately if any module accesses another module's internal package or introduces a cycle.
 */
class ModularityTests {

    @Test
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(DigiyakApplication.class);
        modules.verify();
    }
}
