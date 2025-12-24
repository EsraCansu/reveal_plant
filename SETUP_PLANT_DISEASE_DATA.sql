-- ============================================================
-- PLANT VİLLAGE - PLANT VE DISEASE DATA SETUP
-- Dataset: PlantVillage ResNet101 (38 Classes, 14 Plants)
-- ============================================================

-- Tüm eski verileri temizle (opsiyonel - DIKKAT: bu production'da yapılmamalı)
-- DELETE FROM [Prediction_disease]
-- DELETE FROM [Prediction_plant]
-- DELETE FROM [Disease]
-- DELETE FROM [Plant]

-- ============================================================
-- PLANT TABLOSUNU DOLDUR (14 Bitki)
-- ============================================================
INSERT INTO [Plant] (plant_name, scientific_name, description, valid_classification, image_url) VALUES
('Apple', 'Malus domestica', 'Apple tree - deciduous fruit tree', 1, NULL),
('Blueberry', 'Vaccinium', 'Blueberry bush - berry producing plant', 1, NULL),
('Cherry', 'Prunus', 'Cherry tree - stone fruit tree', 1, NULL),
('Corn', 'Zea mays', 'Corn plant - staple crop', 1, NULL),
('Grape', 'Vitis vinifera', 'Grapevine - climbing plant producing grapes', 1, NULL),
('Orange', 'Citrus sinensis', 'Orange tree - citrus fruit tree', 1, NULL),
('Peach', 'Prunus persica', 'Peach tree - stone fruit tree', 1, NULL),
('Pepper', 'Capsicum annuum', 'Bell pepper plant - vegetable crop', 1, NULL),
('Potato', 'Solanum tuberosum', 'Potato plant - tuber crop', 1, NULL),
('Raspberry', 'Rubus idaeus', 'Raspberry bush - berry producing plant', 1, NULL),
('Soybean', 'Glycine max', 'Soybean plant - legume crop', 1, NULL),
('Squash', 'Cucurbita', 'Squash plant - vegetable crop', 1, NULL),
('Strawberry', 'Fragaria', 'Strawberry plant - berry producing plant', 1, NULL),
('Tomato', 'Solanum lycopersicum', 'Tomato plant - vegetable crop', 1, NULL);

-- ============================================================
-- DISEASE TABLOSUNU DOLDUR (38 Hastalık)
-- ============================================================

-- APPLE DİSEASES (4 - 3 hastalık + 1 healthy)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Apple scab', (SELECT plant_id FROM Plant WHERE plant_name = 'Apple'), 'Olive-colored spots on leaves and fruit, darkening and cracking', 'Fungus: Venturia inaequalis', 'Apply fungicides, prune affected branches, improve air circulation', 85),
('Black rot', (SELECT plant_id FROM Plant WHERE plant_name = 'Apple'), 'Dark, sunken lesions on fruit and branches, black fruiting bodies', 'Fungus: Botryosphaeria obtusa', 'Remove infected fruit and branches, apply fungicides', 80),
('Cedar apple rust', (SELECT plant_id FROM Plant WHERE plant_name = 'Apple'), 'Yellow-orange spots with horn-like projections on fruit', 'Fungus: Gymnosporangium juniperi-virginianae', 'Remove galls from cedar trees, apply fungicides', 75),
('Apple healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Apple'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95);

-- BLUEBERRY DISEASES (1 - healthy)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Blueberry healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Blueberry'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95);

-- CHERRY DISEASES (2)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Cherry healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Cherry'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95),
('Powdery mildew', (SELECT plant_id FROM Plant WHERE plant_name = 'Cherry'), 'White powdery coating on leaves, shoots, and fruit', 'Fungus: Podosphaera clandestina', 'Apply sulfur or fungicides, improve air circulation, prune affected areas', 88);

-- CORN DISEASES (4)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Cercospora leaf spot Gray leaf spot', (SELECT plant_id FROM Plant WHERE plant_name = 'Corn'), 'Gray rectangular lesions on leaves, progressive leaf death', 'Fungus: Cercospora zeae-maydis', 'Use resistant varieties, fungicide application, crop rotation', 82),
('Common rust', (SELECT plant_id FROM Plant WHERE plant_name = 'Corn'), 'Small reddish-brown pustules on leaf surfaces', 'Fungus: Puccinia sorghi', 'Use resistant varieties, fungicides if severe', 78),
('Corn healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Corn'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95),
('Northern Leaf Blight', (SELECT plant_id FROM Plant WHERE plant_name = 'Corn'), 'Long, narrow elliptical lesions on leaves with tan to gray color', 'Fungus: Exserohilum turcicum', 'Use resistant hybrids, fungicide application, crop rotation', 85);

-- GRAPE DISEASES (4)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Black rot', (SELECT plant_id FROM Plant WHERE plant_name = 'Grape'), 'Brown circular lesions on leaves, berries turn black and mummify', 'Fungus: Guignardia bidwellii', 'Remove infected tissues, fungicide application, improve air circulation', 83),
('Esca (Black Measles)', (SELECT plant_id FROM Plant WHERE plant_name = 'Grape'), 'Apoplexy symptoms, white spots inside berries, sudden vine death', 'Fungus: Phaeoacremonium and Fomitiporia species', 'Pruning of infected wood, fungicide application', 80),
('Grape healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Grape'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95),
('Leaf blight (Isariopsis Leaf Spot)', (SELECT plant_id FROM Plant WHERE plant_name = 'Grape'), 'Small reddish spots on leaves that enlarge with brown centers', 'Fungus: Phomopsis viticola', 'Fungicide application, remove affected leaves, improve air circulation', 76);

-- ORANGE DISEASES (1)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Haunglongbing (Citrus greening)', (SELECT plant_id FROM Plant WHERE plant_name = 'Orange'), 'Yellowing of leaves, misshapen fruit, bitter taste, tree decline', 'Bacterium: Candidatus Liberibacter', 'Remove infected trees, control psyllid vectors, antibiotics research ongoing', 90);

-- PEACH DISEASES (2)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Bacterial spot', (SELECT plant_id FROM Plant WHERE plant_name = 'Peach'), 'Small dark lesions on leaves, fruit, and twigs', 'Bacterium: Xanthomonas pruni', 'Copper fungicides, remove infected tissues, resistant varieties', 81),
('Peach healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Peach'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95);

-- PEPPER DISEASES (2)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Bacterial spot', (SELECT plant_id FROM Plant WHERE plant_name = 'Pepper'), 'Small dark lesions on leaves and fruit with yellow halo', 'Bacterium: Xanthomonas campestris', 'Copper fungicides, remove infected plants, resistant varieties', 79),
('Pepper healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Pepper'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95);

-- POTATO DISEASES (3)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Early blight', (SELECT plant_id FROM Plant WHERE plant_name = 'Potato'), 'Brown concentric lesions on lower leaves, progressive defoliation', 'Fungus: Alternaria solani', 'Fungicide application, remove affected foliage, crop rotation', 87),
('Potato healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Potato'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95),
('Late blight', (SELECT plant_id FROM Plant WHERE plant_name = 'Potato'), 'Water-soaked lesions on leaves with white mycelium underneath, rapid progression', 'Fungus: Phytophthora infestans', 'Fungicides, resistant varieties, remove affected plants, improve air circulation', 92);

-- RASPBERRY DISEASES (1)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Raspberry healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Raspberry'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95);

-- SOYBEAN DISEASES (1)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Soybean healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Soybean'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95);

-- SQUASH DISEASES (1)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Powdery mildew', (SELECT plant_id FROM Plant WHERE plant_name = 'Squash'), 'White powdery coating on leaves and stems', 'Fungus: Podosphaera xanthii', 'Sulfur or fungicide application, improve air circulation, resistant varieties', 86);

-- STRAWBERRY DISEASES (2)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Strawberry healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Strawberry'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95),
('Leaf scorch', (SELECT plant_id FROM Plant WHERE plant_name = 'Strawberry'), 'Red to purple lesions on leaf margins, leaf drying and death', 'Fungus: Diplocarpon earlianum', 'Remove infected leaves, fungicide application, improve air circulation', 77);

-- TOMATO DISEASES (10)
INSERT INTO [Disease] (disease_name, plant_id, symptom_description, cause, treatment, confidence) VALUES
('Bacterial spot', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Small dark lesions on leaves and fruit with yellow halo', 'Bacterium: Xanthomonas campestris', 'Copper fungicides, resistant varieties, crop rotation', 84),
('Early blight', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Brown concentric lesions on lower leaves, progressive defoliation', 'Fungus: Alternaria solani', 'Fungicides, prune lower leaves, crop rotation', 89),
('Tomato healthy', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'No visible disease symptoms', 'Healthy plant', 'Maintain good cultivation practices', 95),
('Late blight', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Water-soaked lesions on leaves with white mycelium, fruit rot', 'Fungus: Phytophthora infestans', 'Fungicides, resistant varieties, remove affected plants', 91),
('Leaf Mold', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Yellow spots on upper leaves, grayish mold on undersurface', 'Fungus: Passalora fulva', 'Improve ventilation, fungicides, resistant varieties', 81),
('Septoria leaf spot', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Small circular lesions with dark border and gray center with black dots', 'Fungus: Septoria lycopersici', 'Remove infected leaves, fungicides, crop rotation', 80),
('Spider mites Two-spotted spider mite', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Fine webbing on leaves, yellowing, stippled appearance', 'Pest: Tetranychus urticae', 'Miticides, water spray, predatory mites, resistant varieties', 78),
('Target Spot', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Concentric circular lesions with halo on leaves and fruit', 'Fungus: Corynespora cassiicola', 'Fungicides, resistant varieties, improve air circulation', 76),
('Tomato mosaic virus', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Mottling, mosaic pattern, leaf distortion, stunted growth', 'Virus: Tobacco Mosaic Virus (TMV)', 'Remove infected plants, disinfect tools, resistant varieties', 85),
('Tomato Yellow Leaf Curl Virus', (SELECT plant_id FROM Plant WHERE plant_name = 'Tomato'), 'Yellowing and curling of leaves, stunted growth, reduced fruit', 'Virus: Tomato Yellow Leaf Curl Virus (TYLCV)', 'Control whitefly vectors, resistant varieties, remove infected plants', 87);

-- ============================================================
-- VERIFICATION - Şu anki veri sayısını kontrol et
-- ============================================================
SELECT 'Plant Records' as Entity, COUNT(*) as Count FROM Plant
UNION ALL
SELECT 'Disease Records' as Entity, COUNT(*) as Count FROM Disease;

-- ============================================================
-- END OF DATA SETUP
-- ============================================================
