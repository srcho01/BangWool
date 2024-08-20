package Backend.BangWool.cosmetics.repository;

import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CosmeticsRepository extends JpaRepository<CosmeticsEntity, Long> {
    Optional<CosmeticsEntity> findByMemberAndName(MemberEntity member, String name);
}
