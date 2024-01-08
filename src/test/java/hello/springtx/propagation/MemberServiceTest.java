package hello.springtx.propagation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    @Test
    void testSuccess() {
        String username = "success";
        memberService.joinV1(username);

        assertThat(memberRepository.find(username).get().getUsername()).isEqualTo("success");
        assertThat(logRepository.find(username).get().getMessage()).isEqualTo("success");
    }

    @Test
    void testFail() {

        String username = "로그예외fail";

        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    @Test
    void recoverException_fail() {
        /**
         * 논리 트랜잭션 중 하나라도 롤백되면 전체 트랜잭션은 롤백된다.
         * 내부 트랜잭션이 롤백 되었는데, 외부 트랜잭션이 커밋되면 `UnexpectedRollbackException` 예외가 발생한다.
         * `rollbackOnly` 상황에서 커밋이 발생하면 `UnexpectedRollbackException` 예외가 발생한다.
         */

        String username = "로그예외fail";

       assertThatThrownBy(() -> memberService.joinV2(username)).isInstanceOf(UnexpectedRollbackException.class);

        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    @Test
    void recoverException_success() {

        /**
         * REQUIRES_NEW` 를 사용하게 되면 물리 트랜잭션 자체가 완전히 분리되어 버린다.
         * 그리고 `REQUIRES_NEW` 는 신규 트랜잭션이므로 `rollbackOnly` 표시가 되지 않는다. 그냥 해당 트랜잭션이
         * 물리 롤백되고 끝난다.
         * LogRepository` 에서 예외가 발생한다. 예외를 던지면 `LogRepository` 의 트랜잭션 AOP가 해당 예외를 받는다.
         * `REQUIRES_NEW` 를 사용한 신규 트랜잭션이므로 물리 트랜잭션을 롤백한다. 물리 트랜잭션을 롤백했으므로
         * `rollbackOnly` 를 표시하지 않는다. 여기서 `REQUIRES_NEW` 를 사용한 물리 트랜잭션은 롤백되고 완전히 끝
         * 이 나버린다.
         * 이후 트랜잭션 AOP는 전달 받은 예외를 밖으로 던진다.
         * 예외가 `MemberService` 에 던져지고, `MemberService` 는 해당 예외를 복구한다. 그리고 정상적으로 리턴한다.
         * 정상 흐름이 되었으므로 `MemberService` 의 트랜잭션 AOP는 커밋을 호출한다.
         * 커밋을 호출할 때 신규 트랜잭션이므로 실제 물리 트랜잭션을 커밋해야 한다. 이때 `rollbackOnly` 를 체크한다.
         * `rollbackOnly` 가 없으므로 물리 트랜잭션을 커밋한다.
         * 이후 정상 흐름이 반환된다.
         * **결과적으로 회원 데이터는 저장되고, 로그 데이터만 롤백 되는 것을 확인할 수 있다.**
         */
        String username = "로그예외fail";

       memberService.joinV2(username);

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }
}