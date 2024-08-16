package Backend.BangWool.cosmetics.repository;

import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CosmeticsRepository extends JpaRepository<CosmeticsEntity, Long> {
}
