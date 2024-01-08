package hello.springtx.propagation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class LogRepository {

    private final EntityManager em;

    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public void save(Log logg) {
        log.info("로그 저장");
        em.persist(logg);

        /**
         * JPA를 통한 모든 데이터 변경(등록, 수정, 삭제)에는 트랜잭션이 필요하다. (조회는 트랜잭션 없이 가능하다.)
         */

        if (logg.getMessage().contains("로그예외")) {
            throw new RuntimeException("예외 발생");
        }
    }

    public Optional<Log> find(String message) {
        return em.createQuery("select l from Log l where l.message =: message", Log.class)
                .setParameter("message", message)
                .getResultList().stream().findAny();
    }
}
