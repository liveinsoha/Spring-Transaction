package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Slf4j
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {

        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("커밋 시작");
        txManager.commit(status);
        log.info("커밋 완료");
    }

    @Test
    void rollBack() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("롤백 시작");
        txManager.rollback(status);
        log.info("롤백 완료");
    }

    @Test
    void double_commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status1 = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("커밋 시작");
        txManager.commit(status1);
        log.info("커밋 완료");

        log.info("트랜잭션 시작");
        TransactionStatus status2 = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("커밋 시작");
        txManager.commit(status2);
        log.info("커밋 완료");
    }

    @Test
    void inner_commit() {
        log.info("외부 트래잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction() = {}", outer.isNewTransaction());

        log.info("내부 트래잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner.isNewTransaction() = {}", inner.isNewTransaction());
        log.info("내부 트래잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트래잭션 커밋");
        txManager.commit(outer);
    }

    @Test
    void inner_rollback() {
        log.info("외부 트래잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction() = {}", outer.isNewTransaction());

        log.info("내부 트래잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner.isNewTransaction() = {}", inner.isNewTransaction());
        log.info("내부 트래잭션 롤백");
        txManager.rollback(inner);

        log.info("외부 트래잭션 커밋");
        assertThatThrownBy(() -> txManager.commit(outer)).isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_requires_new() {
        log.info("외부 트래잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTransaction() = {}", outer.isNewTransaction());

        log.info("내부 트래잭션 시작");
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNewTransaction() = {}", inner.isNewTransaction());
        log.info("내부 트래잭션 롤백");
        txManager.rollback(inner);

        log.info("외부 트래잭션 커밋");
       txManager.commit(outer);
    }

}
