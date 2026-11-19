package br.dev.ampliar.caipora.repos;

import br.dev.ampliar.caipora.domain.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;


public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByUsernameAndClientIp(String username, String clientIp);

    @Modifying
    @Query("DELETE FROM LoginAttempt a WHERE a.username = :username")
    int deleteAllByUsername(@Param("username") String username);

    @Modifying
    @Query("DELETE FROM LoginAttempt a WHERE a.lastFailure < :threshold")
    int deleteAllExpired(@Param("threshold") Instant threshold);
}
