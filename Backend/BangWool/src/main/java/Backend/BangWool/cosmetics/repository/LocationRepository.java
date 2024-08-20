package Backend.BangWool.cosmetics.repository;

import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    boolean existsByMemberAndName(MemberEntity member, String name);
    Optional<LocationEntity> findByMemberAndName(MemberEntity member, String name);

}
