package org.delcom.ifs25011p5pbo2026springbootcashflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Smoke test: konteks Spring (dengan H2 lewat profil test) bisa start. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {
    @Test
    void contextLoads() {
    }
}
