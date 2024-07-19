package Backend.BangWool.member.repository;

import Backend.BangWool.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    MemberEntity findByEmail(String email);
    boolean existsByGoogle(String google);
    boolean existsByKakao(String kakao);

}