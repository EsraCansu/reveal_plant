package plant_village.util;

import plant_village.model.Plant;
import plant_village.model.Disease;
import plant_village.repository.PlantRepository;
import plant_village.repository.DiseaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hash Map based Cache Manager for O(1) Plant and Disease lookups
 * Reduces database queries significantly
 * 
 * Architecture: Hash Map (O(1) time complexity)
 * - Plant cache by name and by ID
 * - Disease cache by name and by ID
 * 
 * Benefits:
 * - Eliminates repeated database queries
 * - Improves response time significantly
 * - Reduces database load
 * - Thread-safe using ConcurrentHashMap
 */
@Slf4j
@Component
public class PlantDiseaseCacheManager {

    private final PlantRepository plantRepository;
    private final DiseaseRepository diseaseRepository;
    
    // Hash Maps for O(1) lookup - Thread-safe
    private final Map<String, Plant> plantCache = new ConcurrentHashMap<>();
    private final Map<Integer, Plant> plantIdCache = new ConcurrentHashMap<>();
    private final Map<String, Disease> diseaseCache = new ConcurrentHashMap<>();
    private final Map<Integer, Disease> diseaseIdCache = new ConcurrentHashMap<>();

    @Autowired
    public PlantDiseaseCacheManager(PlantRepository plantRepository, 
                                   DiseaseRepository diseaseRepository) {
        this.plantRepository = plantRepository;
        this.diseaseRepository = diseaseRepository;
        initializeCache();
    }

    /**
     * Initialize cache on startup (load all plants and diseases)
     * This is called automatically when the component is instantiated
     */
    public void initializeCache() {
        log.info("🔄 Initializing Plant and Disease cache...");
        
        try {
            // Load all plants into cache
            plantRepository.findAll().forEach(plant -> {
                plantCache.put(plant.getPlantName(), plant);
                plantIdCache.put(plant.getId(), plant);
            });
            
            // Load all diseases into cache
            diseaseRepository.findAll().forEach(disease -> {
                diseaseCache.put(disease.getName(), disease);
                diseaseIdCache.put(disease.getId(), disease);
            });
            
            log.info("✅ Cache initialized successfully!");
            log.info("   📊 Plants in cache: {}", plantCache.size());
            log.info("   📊 Diseases in cache: {}", diseaseCache.size());
            
        } catch (Exception e) {
            log.error("❌ Error initializing cache", e);
        }
    }

    /**
     * Get Plant by name - O(1) operation
     * Returns Optional<Plant> for safe null handling
     * 
     * @param name Plant name (label from ML model)
     * @return Optional containing Plant if found
     */
    public Optional<Plant> getPlantByName(String name) {
        if (name == null || name.isEmpty()) {
            log.warn("⚠️ Plant name is null or empty");
            return Optional.empty();
        }
        
        // Try to get from cache first (O(1))
        Plant plant = plantCache.get(name);
        
        // If not in cache, fetch from database and cache it
        if (plant == null) {
            log.debug("📍 Cache miss for plant: '{}'. Fetching from database...", name);
            Optional<Plant> dbPlant = plantRepository.findByPlantName(name);
            if (dbPlant.isPresent()) {
                plant = dbPlant.get();
                // Store in both caches for quick future access
                plantCache.put(name, plant);
                plantIdCache.put(plant.getId(), plant);
                log.debug("✅ Plant '{}' cached after DB fetch", name);
            } else {
                log.warn("⚠️ Plant '{}' not found in database", name);
            }
        } else {
            log.debug("✅ Cache hit for plant: '{}'", name);
        }
        
        return Optional.ofNullable(plant);
    }

    /**
     * Get Plant by ID - O(1) operation
     * 
     * @param id Plant ID
     * @return Optional containing Plant if found
     */
    public Optional<Plant> getPlantById(Integer id) {
        if (id == null || id <= 0) {
            log.warn("⚠️ Invalid plant ID: {}", id);
            return Optional.empty();
        }
        
        Plant plant = plantIdCache.get(id);
        
        if (plant == null) {
            log.debug("📍 Cache miss for plant ID: {}. Fetching from database...", id);
            Optional<Plant> dbPlant = plantRepository.findById(id);
            if (dbPlant.isPresent()) {
                plant = dbPlant.get();
                plantCache.put(plant.getPlantName(), plant);
                plantIdCache.put(id, plant);
                log.debug("✅ Plant ID {} cached after DB fetch", id);
            } else {
                log.warn("⚠️ Plant ID {} not found in database", id);
            }
        } else {
            log.debug("✅ Cache hit for plant ID: {}", id);
        }
        
        return Optional.ofNullable(plant);
    }

    /**
     * Get Disease by name - O(1) operation
     * 
     * @param name Disease name (label from ML model)
     * @return Optional containing Disease if found
     */
    public Optional<Disease> getDiseaseByName(String name) {
        if (name == null || name.isEmpty()) {
            log.warn("⚠️ Disease name is null or empty");
            return Optional.empty();
        }
        
        // Try to get from cache first (O(1))
        Disease disease = diseaseCache.get(name);
        
        // If not in cache, fetch from database and cache it
        if (disease == null) {
            log.debug("📍 Cache miss for disease: '{}'. Fetching from database...", name);
            Optional<Disease> dbDisease = diseaseRepository.findByNameIgnoreCase(name);
            if (dbDisease.isPresent()) {
                disease = dbDisease.get();
                // Store in both caches
                diseaseCache.put(name, disease);
                diseaseIdCache.put(disease.getId(), disease);
                log.debug("✅ Disease '{}' cached after DB fetch", name);
            } else {
                log.warn("⚠️ Disease '{}' not found in database", name);
            }
        } else {
            log.debug("✅ Cache hit for disease: '{}'", name);
        }
        
        return Optional.ofNullable(disease);
    }

    /**
     * Get Disease by ID - O(1) operation
     * 
     * @param id Disease ID
     * @return Optional containing Disease if found
     */
    public Optional<Disease> getDiseaseById(Integer id) {
        if (id == null || id <= 0) {
            log.warn("⚠️ Invalid disease ID: {}", id);
            return Optional.empty();
        }
        
        Disease disease = diseaseIdCache.get(id);
        
        if (disease == null) {
            log.debug("📍 Cache miss for disease ID: {}. Fetching from database...", id);
            Optional<Disease> dbDisease = diseaseRepository.findById(id);
            if (dbDisease.isPresent()) {
                disease = dbDisease.get();
                diseaseCache.put(disease.getName(), disease);
                diseaseIdCache.put(id, disease);
                log.debug("✅ Disease ID {} cached after DB fetch", id);
            } else {
                log.warn("⚠️ Disease ID {} not found in database", id);
            }
        } else {
            log.debug("✅ Cache hit for disease ID: {}", id);
        }
        
        return Optional.ofNullable(disease);
    }

    /**
     * Clear entire cache (useful for refresh or admin operations)
     */
    public void clearCache() {
        log.info("🗑️ Clearing all caches...");
        int plantCount = plantCache.size() + plantIdCache.size();
        int diseaseCount = diseaseCache.size() + diseaseIdCache.size();
        
        plantCache.clear();
        plantIdCache.clear();
        diseaseCache.clear();
        diseaseIdCache.clear();
        
        log.info("✅ Cache cleared");
        log.info("   Cleared {} plant references and {} disease references", plantCount / 2, diseaseCount / 2);
    }

    /**
     * Refresh cache (clear and reload all data)
     */
    public void refreshCache() {
        log.info("🔄 Refreshing cache...");
        clearCache();
        initializeCache();
        log.info("✅ Cache refresh completed");
    }

    /**
     * Get cache statistics for monitoring
     */
    public void printCacheStats() {
        log.info("📊 ===== Cache Statistics =====");
        log.info("  Plants in cache: {}", plantCache.size());
        log.info("  Plant IDs in cache: {}", plantIdCache.size());
        log.info("  Diseases in cache: {}", diseaseCache.size());
        log.info("  Disease IDs in cache: {}", diseaseIdCache.size());
        log.info("  Total cache entries: {}", 
                 plantCache.size() + plantIdCache.size() + diseaseCache.size() + diseaseIdCache.size());
        log.info("==============================");
    }

    /**
     * Get cache hit ratio information (optional - for advanced monitoring)
     */
    public Map<String, Integer> getCacheStats() {
        return Map.of(
            "plants", plantCache.size(),
            "plantIds", plantIdCache.size(),
            "diseases", diseaseCache.size(),
            "diseaseIds", diseaseIdCache.size()
        );
    }
}
