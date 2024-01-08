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
public class TxLevelTest {

    @Autowired
    LevelService service;

    @Test
    void levelTest() {
        service.write();
        service.read();

    }

    @TestConfiguration
    static class config {

        @Bean
        public LevelService levelService() {
            return new LevelService();
        }
    }

    @Transactional(readOnly = true)
    static class LevelService {

        /**
         * 클래스에 적용하면 메서드는 자동 적용**
         * `read()` : 해당 메서드에 `@Transactional` 이 없다. 이 경우 더 상위인 클래스를 확인한다.
         * 클래스에 `@Transactional(readOnly = true)` 이 적용되어 있다. 따라서 트랜잭션이 적용되고
         * `readOnly = true` 옵션을 사용하게 된다.
         * 참고로 `readOnly=false` 는 기본 옵션이기 때문에 보통 생략한다. 여기서는 이해를 돕기 위해 기본 옵션을 적어주었
         * 다.
         */

        @Transactional(readOnly = false)
        public void write() {
            log.info("call write");
            printTxInfo();
        }

        public void read() {
            log.info("call read");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly = {}", readOnly);
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", active);
        }
    }
}
