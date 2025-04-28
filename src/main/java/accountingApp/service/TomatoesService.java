package accountingApp.service;

import accountingApp.entity.TomatoesCategory;
import accountingApp.entity.Tomatoes;
import accountingApp.entity.dto.devicesdto.MaxOwnerCountDTO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import accountingApp.repository.TomatoesRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TomatoesService {
    @Autowired
    TomatoesRepository tomatoesRepository;

    public List<Tomatoes> findAllTomatoes() {
        return tomatoesRepository.findAll();
    }

    public void addNewTomato(Tomatoes tomato) {
        tomatoesRepository.save(tomato);
    }

    public void deleteTomato(Tomatoes tomato) {
        tomatoesRepository.delete(tomato);
    }

//
//    public List<Tomatoes> getDevicesByName(String name) {
//
//        List<Tomatoes> cloneDevices = tomatoesRepository.findAll().stream()
//        .filter(dev -> dev.getName().contains(name))
//        .collect(Collectors.toList());
//
//
//        if (!cloneDevices.isEmpty()) {
//            return cloneDevices;
//        } else {
//            return tomatoesRepository.findAll();
//        }
//    }
//
//    public List<Tomatoes> getDevicesById(ObjectId id) {
//        return tomatoesRepository.findByid(id);
//    }
//
//    public List<Tomatoes> getDevicesByCategory(TomatoesCategory category) {
//        return tomatoesRepository.findByCategory(category);
//    }
//
//    public List<MaxOwnerCountDTO> getOwnersCount(){
//        return tomatoesRepository.reportingDevicesMaxOwnerCount();
//    }
}
