package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    /**
     * `@Transactional` 을 메서드나 클래스에 붙이면 해당 객체는 트랜잭션 AOP 적용의 대상이 되고, 결과적으로
     * 실제 객체 대신에 트랜잭션을 처리해주는 프록시 객체가 스프링 빈에 등록된다. 그리고 주입을 받을 때도 실제 객
     * 체 대신에 프록시 객체가 주입된다.
     * 그리고 실제 `basicService` 객체 대신에 프록시인 `basicService$
     * $CGLIB` 를 스프링 빈에 등록한다. 그리고 프록시는 내부에 실제 `basicService` 를 참조하게 된다. 여기서 핵
     * 심은 실제 객체 대신에 프록시가 스프링 컨테이너에 등록되었다는 점이다.
     * 클라이언트인 `txBasicTest` 는 스프링 컨테이너에 `@Autowired BasicService basicService` 로 의
     * 존관계 주입을 요청한다. 스프링 컨테이너에는 실제 객체 대신에 프록시가 스프링 빈으로 등록되어 있기 때문에
     * 프록시를 주입한다.
     * 프록시는 `BasicService` 를 상속해서 만들어지기 때문에 다형성을 활용할 수 있다. 따라서 `BasicService`
     * 대신에 프록시인 `BasicService$$CGLIB` 를 주입할 수 있다.
     */
    @Autowired
    BasicService basicService;

    /**
     * 로그를 통해 `tx()` 호출시에는 `tx active=true` 를 통해 트랜잭션이 적용된 것을 확인할 수 있다.
     * `TransactionInterceptor` 로그를 통해 트랜잭션 프록시가 트랜잭션을 시작하고 완료한 내용을 확인할 수
     * 있다.
     * `nonTx()` 호출시에는 `tx active=false` 를 통해 트랜잭션이 없는 것을 확인할 수 있다.
     */
    @Test
    void proxyTest() {

        log.info("basicService.getClass() = {}", basicService.getClass());
        assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }

    @Test
    void txTest() {
        basicService.tx();
        basicService.nonTx();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public BasicService basicService() {
            return new BasicService();
        }
    }


    static class BasicService {

        @Transactional
        public void tx() {
            log.info("call tx");
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", active);

        }

        public void nonTx() {
            log.info("call nonTx");
            boolean active = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", active);
        }
    }
}
