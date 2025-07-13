package accountingApp.service;


import accountingApp.entity.Tomatoes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import accountingApp.repository.TomatoesRepository;

import java.util.List;


@Service
public class TomatoesService {

    private final TomatoesRepository tomatoesRepository;

    @Autowired
    public TomatoesService(TomatoesRepository tomatoesRepository) {
        this.tomatoesRepository = tomatoesRepository;
    }

    public List<Tomatoes> findAllTomatoes() {
        return tomatoesRepository.findAll();
    }

    public void addNewTomato(Tomatoes tomato) {
        tomatoesRepository.save(tomato);
    }

    public void deleteTomato(Tomatoes tomato) {
        tomatoesRepository.delete(tomato);
    }

    public Tomatoes getTomatoById(String id) {

        return tomatoesRepository.findAll().stream()
                .filter(tomId -> (tomId.getId() + "").equals(id))
                .findFirst()
                .orElse(new Tomatoes());
    }
}
