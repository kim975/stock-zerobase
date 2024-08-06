package zerobase.stock.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.stock.persist.entity.MemberEntity;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

    Optional<MemberEntity> findByUsername(String username);

    boolean existsByUsername(String username);

}
