package accountingApp.repository;

import accountingApp.entity.TomatirriUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<TomatirriUser, Long> {

    @Override
    List<TomatirriUser> findAll();

    Optional<TomatirriUser> findByUserName(String userName);

    List<TomatirriUser> findAppUserById(long id);

    void deleteById(long id);
}
