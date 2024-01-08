package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService callService; //내부 메소드에 Transactional이 걸려있어서 프록시 객체는 맞다.

    @Test
    void external() {
        callService.external();
    }

    @Test
    void internal() {
        callService.internal();
    }

    @Test
    void test() {
        log.info("callService.getClass() = {}", callService.getClass());
    }

    @TestConfiguration
    static class Config {

        @Bean
        public CallService callService() {
            return new CallService();
        }
    }

    static class CallService {

        public void external() {
            log.info("call external");
            printTxInfo();
            internal();
        }

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", active);
        }
    }
}
