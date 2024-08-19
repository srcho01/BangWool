package Backend.BangWool.cosmetics.repository;

import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    boolean existsByMemberAndName(MemberEntity member, String name);
}
