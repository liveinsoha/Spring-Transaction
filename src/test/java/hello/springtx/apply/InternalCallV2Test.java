package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
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
public class InternalCallV2Test {

    /**
     * 1. 클라이언트인 테스트 코드는 `callService.external()` 을 호출한다.
     * 2. `callService` 는 실제 `callService` 객체 인스턴스이다.
     * 3. `callService` 는 주입 받은 `internalService.internal()` 을 호출한다.
     * 4. `internalService` 는 트랜잭션 프록시이다. `internal()` 메서드에 `@Transactional` 이 붙어 있으
     * 므로 트랜잭션 프록시는 트랜잭션을 적용한다.
     * 5. 트랜잭션 적용 후 실제 `internalService` 객체 인스턴스의 `internal()` 을 호출한다.
     */

    @Autowired
    CallService callService;

    @Test
    void test() {
        callService.external();
    }

    @TestConfiguration
    static class Config {

        @Bean
        public CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        public InternalService internalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("call external");
            printTxInfo();
            internalService.internal();
        }

        private void printTxInfo() {
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", active);
        }


    }

    @Transactional
    @Slf4j
    static class InternalService {

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
