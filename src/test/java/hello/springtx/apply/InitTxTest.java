package hello.springtx.apply;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InitTxTest {

    @Autowired
    Hello hello;

    @Test
    void test() {

    }


    @TestConfiguration
    static class Config {

        @Bean
        public Hello hello() {
            return new Hello();
        }
    }

    @Slf4j
    static class Hello {

        @Transactional
        @PostConstruct
        public void initV1() {
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("initV1 @PostConstruct Tx active = {}", active);
        }

        @Transactional
        @EventListener(ApplicationReadyEvent.class)
        public void initV2() {
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("initV1 ApplicationReadyEvent Tx active = {}", active);
        }
    }
}
