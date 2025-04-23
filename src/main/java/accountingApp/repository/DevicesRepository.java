package accountingApp.repository;

import accountingApp.entity.*;
import accountingApp.entity.dto.devicesdto.MaxOwnerCountDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Интерфейс для связи с таблицей оборудования (Devices) в БД
 */

@Repository
public interface DevicesRepository extends JpaRepository<Tomatoes, Integer> {
    @Override
    List<Tomatoes> findAll();

    List<Tomatoes> findByid(int id);

    List<Tomatoes> findByName(String name);

    @Override
    void deleteById(Integer integer);

    @Query(value = "SELECT d from Tomatoes d where d.category = ?1")
    List<Tomatoes> findByCategory(DeviceCategory category);

    @Query(value = "SELECT d from Tomatoes d where d.description = ?1")
    List<Tomatoes> findByDescription(String description);

    @Query(value = "SELECT d from Tomatoes d where d.inventory = ?1")
    List<Tomatoes> findByInventory(Long inventory);

    @Query(value = "SELECT d from Tomatoes d where d.serial = ?1")
    List<Tomatoes> findBySerial(String serial);

    @Query(value = "SELECT d from Tomatoes d where d.room = ?1")
    List<Tomatoes> findByRoom(Room room);

    @Query(value = "SELECT d from Tomatoes d where d.employee = ?1")
    List<Tomatoes> findByEmployee(Employee employee);

    @Query(value = "SELECT d from Tomatoes d where d.itstaff = ?1")
    List<Tomatoes> findByItStaff(ITStaff itstaff);

    @Query(value = "SELECT d from Tomatoes d where d.repair = ?1")
    List<Tomatoes> findByRepair(Repair repair);

    @Query(value = "select new accountingApp.entity.dto.devicesdto.MaxOwnerCountDTO(" +
            " i.name, count(i.id)) " +
            " from Tomatoes  d" +
            " join d.itstaff i" +
            " group by i.name")
    List<MaxOwnerCountDTO> reportingDevicesMaxOwnerCount();
}
