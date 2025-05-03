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
public interface TomatoesRepository extends MongoRepository<Tomatoes, Integer> {
    @Override
    List<Tomatoes> findAll();

    @Query("{'tomatoesName' :  {$regex: ?0, $options: 'i' }}")
    List<Tomatoes> findByName(String tomatoesName);

//    @Query(value = "SELECT d from Tomatoes d where d.category = ?1")
//    List<Tomatoes> findByCategory(TomatoesCategory category);
//
//    @Query(value = "SELECT d from Tomatoes d where d.description = ?1")
//    List<Tomatoes> findByDescription(String description);
//
//    @Query(value = "SELECT d from Tomatoes d where d.inventory = ?1")
//    List<Tomatoes> findByInventory(Long inventory);
//
//    @Query(value = "SELECT d from Tomatoes d where d.serial = ?1")
//    List<Tomatoes> findBySerial(String serial);
//
//    @Query(value = "SELECT d from Tomatoes d where d.room = ?1")
//    List<Tomatoes> findByRoom(Room room);
//
//    @Query(value = "SELECT d from Tomatoes d where d.employee = ?1")
//    List<Tomatoes> findByEmployee(Employee employee);
//
//    @Query(value = "SELECT d from Tomatoes d where d.itstaff = ?1")
//    List<Tomatoes> findByItStaff(ITStaff itstaff);
//
//    @Query(value = "SELECT d from Tomatoes d where d.repair = ?1")
//    List<Tomatoes> findByRepair(Repair repair);
//
//    @Query(value = "select new accountingApp.entity.dto.devicesdto.MaxOwnerCountDTO(" +
//            " i.name, count(i.id)) " +
//            " from Tomatoes  d" +
//            " join d.itstaff i" +
//            " group by i.name")
//    List<MaxOwnerCountDTO> reportingDevicesMaxOwnerCount();
}
