package Backend.BangWool.cosmetics.repository;

import Backend.BangWool.cosmetics.domain.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}
