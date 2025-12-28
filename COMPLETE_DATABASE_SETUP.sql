-- ============================================================
-- REVEAL PLANT - COMPLETE DATABASE SETUP
-- ============================================================
-- Bu script tüm tabloları sıfırdan oluşturur
-- SQL Server (MSSQL) için hazırlanmıştır
-- Tarih: 2025-12-28
-- ============================================================

-- Veritabanını oluştur (eğer yoksa)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'plant_village')
BEGIN
    CREATE DATABASE plant_village;
END
GO

USE plant_village;
GO

-- ============================================================
-- 1. USER TABLOSU
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user')
BEGIN
    CREATE TABLE [user] (
        user_id INT IDENTITY(1,1) PRIMARY KEY,
        user_name VARCHAR(50) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        role NVARCHAR(10) DEFAULT 'USER',
        phone VARCHAR(20) NULL,
        location VARCHAR(100) NULL,
        avatar_url VARCHAR(500) NULL,
        bio TEXT NULL,
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        last_login DATETIME2 DEFAULT GETDATE(),
        create_at DATETIME2 DEFAULT GETDATE()
    );
    PRINT 'User table created successfully';
END
GO

-- ============================================================
-- 2. PLANT TABLOSU
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'plant')
BEGIN
    CREATE TABLE plant (
        plant_id INT IDENTITY(1,1) PRIMARY KEY,
        plant_name VARCHAR(50) NOT NULL,
        scientific_name VARCHAR(50) NULL,
        description VARCHAR(MAX) NULL,
        image_url VARCHAR(MAX) NULL,
        care_tips NVARCHAR(MAX) NULL,
        watering_frequency NVARCHAR(50) NULL,
        sunlight_requirement NVARCHAR(100) NULL,
        soil_type NVARCHAR(100) NULL,
        hardiness_zone NVARCHAR(50) NULL,
        valid_classification BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
    PRINT 'Plant table created successfully';
END
GO

-- ============================================================
-- 3. DISEASE TABLOSU
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'disease')
BEGIN
    CREATE TABLE disease (
        disease_id INT IDENTITY(1,1) PRIMARY KEY,
        disease_name VARCHAR(50) NOT NULL,
        cause VARCHAR(MAX) NULL,
        symptom_description VARCHAR(MAX) NULL,
        treatment VARCHAR(MAX) NULL,
        example_image_url VARCHAR(MAX) NULL
    );
    PRINT 'Disease table created successfully';
END
GO

-- ============================================================
-- 4. PREDICTION TABLOSU
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'prediction')
BEGIN
    CREATE TABLE prediction (
        prediction_id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        prediction_type VARCHAR(50) NULL,
        confidence FLOAT NULL,
        uploaded_image_url NVARCHAR(MAX) NULL,
        is_valid BIT DEFAULT 1,
        create_at DATETIME2 DEFAULT GETDATE(),
        watering_frequency NVARCHAR(50) NULL,
        care_tips NVARCHAR(MAX) NULL,
        soil_type NVARCHAR(100) NULL,
        hardiness_zone NVARCHAR(50) NULL,
        CONSTRAINT FK_prediction_user FOREIGN KEY (user_id) REFERENCES [user](user_id)
    );
    PRINT 'Prediction table created successfully';
END
GO

-- ============================================================
-- 5. PREDICTION_PLANT TABLOSU (Junction Table)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'prediction_plant')
BEGIN
    CREATE TABLE prediction_plant (
        id INT IDENTITY(1,1) PRIMARY KEY,
        prediction_id INT NOT NULL,
        plant_id INT NOT NULL,
        match_confidence FLOAT NULL,
        CONSTRAINT FK_prediction_plant_prediction FOREIGN KEY (prediction_id) REFERENCES prediction(prediction_id),
        CONSTRAINT FK_prediction_plant_plant FOREIGN KEY (plant_id) REFERENCES plant(plant_id)
    );
    PRINT 'Prediction_Plant table created successfully';
END
GO

-- ============================================================
-- 6. PREDICTION_DISEASE TABLOSU (Junction Table)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'prediction_disease')
BEGIN
    CREATE TABLE prediction_disease (
        id INT IDENTITY(1,1) PRIMARY KEY,
        prediction_id INT NOT NULL,
        disease_id INT NOT NULL,
        match_confidence FLOAT NULL,
        is_healthy BIT NULL,
        CONSTRAINT FK_prediction_disease_prediction FOREIGN KEY (prediction_id) REFERENCES prediction(prediction_id),
        CONSTRAINT FK_prediction_disease_disease FOREIGN KEY (disease_id) REFERENCES disease(disease_id)
    );
    PRINT 'Prediction_Disease table created successfully';
END
GO

-- ============================================================
-- 7. PREDICTION_LOG TABLOSU
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'prediction_log')
BEGIN
    CREATE TABLE prediction_log (
        log_id INT IDENTITY(1,1) PRIMARY KEY,
        prediction_id INT NOT NULL,
        action_type VARCHAR(50) NOT NULL,
        timestamp DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_prediction_log_prediction FOREIGN KEY (prediction_id) REFERENCES prediction(prediction_id)
    );
    PRINT 'Prediction_Log table created successfully';
END
GO

-- ============================================================
-- 8. PREDICTION_FEEDBACK TABLOSU
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'prediction_feedback')
BEGIN
    CREATE TABLE prediction_feedback (
        feedback_id INT IDENTITY(1,1) PRIMARY KEY,
        prediction_id INT NOT NULL,
        is_correct BIT NOT NULL DEFAULT 0,
        is_approved_from_admin BIT NOT NULL DEFAULT 0,
        image_added_to_db BIT NOT NULL DEFAULT 0,
        comment VARCHAR(500) NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_prediction_feedback_prediction FOREIGN KEY (prediction_id) REFERENCES prediction(prediction_id)
    );
    PRINT 'Prediction_Feedback table created successfully';
END
GO

-- ============================================================
-- ÖRNEK VERİLER - PLANT (14 bitki)
-- ============================================================
IF NOT EXISTS (SELECT * FROM plant WHERE plant_name = 'Apple')
BEGIN
    INSERT INTO plant (plant_name, scientific_name, valid_classification) VALUES
    ('Apple', 'Malus domestica', 1),
    ('Blueberry', 'Vaccinium corymbosum', 1),
    ('Cherry', 'Prunus avium', 1),
    ('Corn', 'Zea mays', 1),
    ('Grape', 'Vitis vinifera', 1),
    ('Orange', 'Citrus sinensis', 1),
    ('Peach', 'Prunus persica', 1),
    ('Pepper', 'Capsicum annuum', 1),
    ('Potato', 'Solanum tuberosum', 1),
    ('Raspberry', 'Rubus idaeus', 1),
    ('Soybean', 'Glycine max', 1),
    ('Squash', 'Cucurbita', 1),
    ('Strawberry', 'Fragaria ananassa', 1),
    ('Tomato', 'Solanum lycopersicum', 1);
    PRINT 'Sample plants inserted';
END
GO

-- ============================================================
-- ÖRNEK VERİLER - DISEASE (38 hastalık)
-- ============================================================
IF NOT EXISTS (SELECT * FROM disease WHERE disease_name = 'Apple___Apple_scab')
BEGIN
    INSERT INTO disease (disease_name, cause, symptom_description, treatment) VALUES
    ('Apple___Apple_scab', 'Venturia inaequalis fungus', 'Dark, scabby lesions on leaves and fruit', 'Fungicide application, remove infected leaves'),
    ('Apple___Black_rot', 'Botryosphaeria obtusa fungus', 'Brown rotting on fruit, leaf spots', 'Prune infected branches, apply fungicide'),
    ('Apple___Cedar_apple_rust', 'Gymnosporangium juniperi-virginianae', 'Orange spots on leaves', 'Remove nearby cedar trees, apply fungicide'),
    ('Apple___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Blueberry___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Cherry___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Cherry___Powdery_mildew', 'Podosphaera clandestina', 'White powdery coating on leaves', 'Apply sulfur-based fungicide'),
    ('Corn___Cercospora_leaf_spot Gray_leaf_spot', 'Cercospora zeae-maydis', 'Gray rectangular lesions on leaves', 'Crop rotation, resistant varieties'),
    ('Corn___Common_rust', 'Puccinia sorghi', 'Reddish-brown pustules on leaves', 'Apply fungicide, plant resistant varieties'),
    ('Corn___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Corn___Northern_Leaf_Blight', 'Exserohilum turcicum', 'Long elliptical gray-green lesions', 'Resistant varieties, crop rotation'),
    ('Grape___Black_rot', 'Guignardia bidwellii', 'Brown circular lesions, fruit rot', 'Remove mummies, apply fungicide'),
    ('Grape___Esca_(Black_Measles)', 'Multiple fungi', 'Tiger stripe pattern on leaves', 'No cure, remove infected vines'),
    ('Grape___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Grape___Leaf_blight_(Isariopsis_Leaf_Spot)', 'Isariopsis clavispora', 'Brown spots with dark borders', 'Fungicide application'),
    ('Orange___Haunglongbing_(Citrus_greening)', 'Candidatus Liberibacter', 'Yellow shoots, misshapen fruit', 'Remove infected trees, control psyllids'),
    ('Peach___Bacterial_spot', 'Xanthomonas arboricola', 'Dark spots on leaves and fruit', 'Copper-based bactericides'),
    ('Peach___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Pepper__bell___Bacterial_spot', 'Xanthomonas campestris', 'Water-soaked spots on leaves', 'Copper sprays, resistant varieties'),
    ('Pepper__bell___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Potato___Early_blight', 'Alternaria solani', 'Dark spots with concentric rings', 'Fungicide, crop rotation'),
    ('Potato___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Potato___Late_blight', 'Phytophthora infestans', 'Water-soaked lesions, white mold', 'Fungicide, destroy infected plants'),
    ('Raspberry___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Soybean___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Squash___Powdery_mildew', 'Podosphaera xanthii', 'White powdery spots on leaves', 'Sulfur-based fungicide, good air circulation'),
    ('Strawberry___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Strawberry___Leaf_scorch', 'Diplocarpon earlianum', 'Purple spots with tan centers', 'Remove infected leaves, fungicide'),
    ('Tomato___Bacterial_spot', 'Xanthomonas species', 'Small dark spots on leaves and fruit', 'Copper-based sprays'),
    ('Tomato___Early_blight', 'Alternaria solani', 'Dark spots with concentric rings', 'Fungicide, remove lower leaves'),
    ('Tomato___healthy', NULL, 'No disease symptoms', 'Continue regular care'),
    ('Tomato___Late_blight', 'Phytophthora infestans', 'Water-soaked spots, white mold', 'Fungicide, destroy infected plants'),
    ('Tomato___Leaf_Mold', 'Passalora fulva', 'Yellow spots above, olive mold below', 'Improve ventilation, fungicide'),
    ('Tomato___Septoria_leaf_spot', 'Septoria lycopersici', 'Small circular spots with dark borders', 'Remove infected leaves, fungicide'),
    ('Tomato___Spider_mites Two-spotted_spider_mite', 'Tetranychus urticae', 'Yellow stippling, webbing on leaves', 'Miticides, increase humidity'),
    ('Tomato___Target_Spot', 'Corynespora cassiicola', 'Brown spots with concentric rings', 'Fungicide, remove infected leaves'),
    ('Tomato___Tomato_mosaic_virus', 'Tomato mosaic virus', 'Mottled light and dark green leaves', 'Remove infected plants, sanitize tools'),
    ('Tomato___Tomato_Yellow_Leaf_Curl_Virus', 'TYLCV', 'Upward curling yellow leaves', 'Control whiteflies, remove infected plants');
    PRINT 'Sample diseases inserted';
END
GO

-- ============================================================
-- ÖRNEK KULLANICI (Test için)
-- ============================================================
IF NOT EXISTS (SELECT * FROM [user] WHERE email = 'test@test.com')
BEGIN
    -- Password: test123 (BCrypt hash)
    INSERT INTO [user] (user_name, email, password_hash, role)
    VALUES ('Test User', 'test@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye0VM8VvFW5.XfvQ3k3iGwrR0QC0HoXGi', 'USER');
    PRINT 'Test user created (email: test@test.com, password: test123)';
END
GO

PRINT '';
PRINT '============================================================';
PRINT 'DATABASE SETUP COMPLETED SUCCESSFULLY!';
PRINT '============================================================';
PRINT 'Tables created: user, plant, disease, prediction,';
PRINT '                prediction_plant, prediction_disease,';
PRINT '                prediction_log, prediction_feedback';
PRINT '';
PRINT 'Sample data: 14 plants, 38 diseases, 1 test user';
PRINT 'Test user: test@test.com / test123';
PRINT '============================================================';
GO
