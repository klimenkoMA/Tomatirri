package accountingApp.service;

import accountingApp.entity.TomatoesCategory;
import accountingApp.entity.Tomatoes;
import accountingApp.entity.dto.devicesdto.MaxOwnerCountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import accountingApp.repository.DevicesRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DevicesService {
    @Autowired
    DevicesRepository devicesRepository;

    public List<Tomatoes> findAllDevices() {
        return devicesRepository.findAll();
    }

    public void addNewDevice(Tomatoes tomatoes) {
        devicesRepository.save(tomatoes);
    }

    public void deleteDeviceById(Integer id) {
        devicesRepository.deleteById(id);
    }

    public void updateDevice(Tomatoes tomatoes) {
        devicesRepository.save(tomatoes);
    }

    public List<Tomatoes> getDevicesByName(String name) {

        List<Tomatoes> cloneDevices = devicesRepository.findAll().stream()
        .filter(dev -> dev.getName().contains(name))
        .collect(Collectors.toList());


        if (!cloneDevices.isEmpty()) {
            return cloneDevices;
        } else {
            return devicesRepository.findAll();
        }
    }

    public List<Tomatoes> getDevicesById(int id) {
        return devicesRepository.findByid(id);
    }

    public List<Tomatoes> getDevicesByCategory(TomatoesCategory category) {
        return devicesRepository.findByCategory(category);
    }

    public List<MaxOwnerCountDTO> getOwnersCount(){
        return devicesRepository.reportingDevicesMaxOwnerCount();
    }
}
