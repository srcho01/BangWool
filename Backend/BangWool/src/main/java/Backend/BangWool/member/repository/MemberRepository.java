package Backend.BangWool.member.repository;

import Backend.BangWool.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<MemberEntity> findByEmail(String email);
    boolean existsByGoogleId(String google);
    boolean existsByKakaoId(String kakao);
    Optional<MemberEntity> findByNameAndBirth(String name, LocalDate birth);
    Optional<MemberEntity> findByEmailAndNameAndBirth(String email, String name, LocalDate birth);

}