package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    /**
     * NotEnoughMoneyException` 은 시스템에 문제가 발생한 것이 아니라, 비즈니스 문제 상황을 예외를 통해 알
     * 려준다. 마치 예외가 리턴 값 처럼 사용된다. 따라서 이 경우에는 트랜잭션을 커밋하는 것이 맞다. 이 경우 롤백하
     * 면 생성한 `Order` 자체가 사라진다. 그러면 고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내해도 주
     * 문( `Order` ) 자체가 사라지기 때문에 문제가 된다.
     * 그런데 비즈니스 상황에 따라 체크 예외의 경우에도 트랜잭션을 커밋하지 않고, 롤백하고 싶을 수 있다. 이때는
     * `rollbackFor` 옵션을 사용하면 된다.
     */

    @Test
    void complete() throws NotEnoughMoneyException {
        Order order = new Order("aa");
        orderService.order(order);

        log.info("르포지토리 조회");
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayState()).isEqualTo("완료");
    }

    @Test
    void notEnoughMoneyException() {
        Order order = new Order("잔고부족");
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(NotEnoughMoneyException.class);

        log.info("르포지토리 조회");
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayState()).isEqualTo("대기");
    }

    @Test
    void runtimeException() {
        Order order = new Order("예외");
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(RuntimeException.class);

        log.info("르포지토리 조회");
        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional.isEmpty()).isTrue();
    }
}