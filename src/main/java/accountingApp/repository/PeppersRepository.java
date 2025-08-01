package accountingApp.repository;


import accountingApp.entity.Peppers;

import com.mongodb.lang.NonNullApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Интерфейс для связи с коллекцией семян перцев (Peppers) в БД
 */


@Repository
public interface PeppersRepository extends MongoRepository<Peppers, String> {

    @Override
    List<Peppers> findAll();

    Page<Peppers> findAll(Pageable pageable);

}
