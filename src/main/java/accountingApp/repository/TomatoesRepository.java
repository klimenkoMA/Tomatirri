package accountingApp.repository;

import accountingApp.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Интерфейс для связи с коллекцией семян томатов (Tomatoes) в БД
 */

@Repository
public interface TomatoesRepository extends MongoRepository<Tomatoes, String> {
    @Override
    List<Tomatoes> findAll();

    Page<Tomatoes> findAll(Pageable pageable);

    @Query("{'tgBotButtonName' :  {$regex: ?0, $options: 'i' }}")
    Tomatoes findTomatoesByShortName(String tomatoesName);

    @Query("{'category' : 'Гном'}")
    List<Tomatoes> findAllByCategoryDwarf();



}
