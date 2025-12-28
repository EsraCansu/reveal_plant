package plant_village.service.impl;

import plant_village.model.Disease;
import plant_village.repository.DiseaseRepository;
import plant_village.service.DiseaseService;
import plant_village.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiseaseServiceImpl implements DiseaseService {
    
    @Autowired
    private DiseaseRepository diseaseRepository;
    
    @Override
    public List<Disease> getAllDiseases() {
        log.info("Listing all diseases");
        return diseaseRepository.findAll();
    }
    
    @Override
    public Optional<Disease> getDiseaseById(Integer id) {
        log.info("Fetching disease - ID: {}", id);
        Optional<Disease> disease = diseaseRepository.findById(id);
        if (disease.isEmpty()) {
            log.warn("Disease not found - ID: {}", id);
            throw new ResourceNotFoundException("Disease not found - ID: " + id);
        }
        return disease;
    }
    
    @Override
    public List<Disease> searchByName(String name) {
        log.info("Searching disease - Name: {}", name);
        List<Disease> diseases = diseaseRepository.findAll().stream()
            .filter(d -> d.getDiseaseName().equalsIgnoreCase(name))
            .collect(Collectors.toList());
        
        if (diseases.isEmpty()) {
            throw new ResourceNotFoundException("Disease not found - Name: " + name);
        }
        return diseases;
    }
    
    @Override
    public List<Disease> searchByKeyword(String keyword) {
        log.info("Searching disease by keyword - Keyword: {}", keyword);
        return diseaseRepository.findAll().stream()
            .filter(d -> d.getDiseaseName().toLowerCase().contains(keyword.toLowerCase()) ||
                        (d.getSymptomDescription() != null && d.getSymptomDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                        (d.getTreatment() != null && d.getTreatment().toLowerCase().contains(keyword.toLowerCase())))
            .collect(Collectors.toList());
    }
    
    @Override
    public Disease createDisease(Disease disease) {
        log.info("Creating new disease - Name: {}", disease.getDiseaseName());
        return diseaseRepository.save(disease);
    }
    
    @Override
    public Disease updateDisease(Integer id, Disease diseaseDetails) {
        log.info("Updating disease - ID: {}", id);
        Disease disease = diseaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Disease not found - ID: " + id));
        
        if (diseaseDetails.getDiseaseName() != null) {
            disease.setDiseaseName(diseaseDetails.getDiseaseName());
        }
        if (diseaseDetails.getSymptomDescription() != null) {
            disease.setSymptomDescription(diseaseDetails.getSymptomDescription());
        }
        if (diseaseDetails.getCause() != null) {
            disease.setCause(diseaseDetails.getCause());
        }
        if (diseaseDetails.getTreatment() != null) {
            disease.setTreatment(diseaseDetails.getTreatment());
        }
        
        return diseaseRepository.save(disease);
    }
    
    @Override
    public void deleteDisease(Integer id) {
        log.info("Deleting disease - ID: {}", id);
        if (!diseaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disease not found - ID: " + id);
        }
        diseaseRepository.deleteById(id);
        log.info("Disease deleted - ID: {}", id);
    }
}
