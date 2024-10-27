package br.dev.ampliar.caipora;

import br.dev.ampliar.caipora.config.DomainConfig;
import br.dev.ampliar.caipora.config.WebFormsSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CaiporaApplication.class, DomainConfig.class, WebFormsSecurityConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
class CaiporaApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }
}