package accountingApp.repository;

import accountingApp.entity.*;
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


//    @Query(value = "select new accountingApp.entity.dto.devicesdto.MaxOwnerCountDTO(" +
//            " i.name, count(i.id)) " +
//            " from Tomatoes  d" +
//            " join d.itstaff i" +
//            " group by i.name")
//    List<MaxOwnerCountDTO> reportingDevicesMaxOwnerCount();
}
