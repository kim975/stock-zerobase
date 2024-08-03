package zerobase.stock.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.stock.persist.entity.DividendEntity;

import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {
    public List<DividendEntity> findAllByCompanyId(Long id);
}
