USE plant_village;

-- Test Users
INSERT INTO [User] (user_name, email, password_hash, created_at, last_login, role)
VALUES 
('testuser', 'test@example.com', 'hashed_pass_123', GETDATE(), GETDATE(), 'USER'),
('ali', 'ali@example.com', 'hashed_ali_123', GETDATE(), GETDATE(), 'USER'),
('admin', 'admin@example.com', 'hashed_admin_123', GETDATE(), GETDATE(), 'ADMIN');

-- Test Plants
INSERT INTO Plant (plant_name, scientific_name, image_url, description, valid_classification)
VALUES 
('Apple', 'Malus domestica', 'https://example.com/apple.jpg', 'Apple tree', 1),
('Tomato', 'Solanum lycopersicum', 'https://example.com/tomato.jpg', 'Tomato plant', 1),
('Corn', 'Zea mays', 'https://example.com/corn.jpg', 'Corn plant', 1);

-- Test Diseases
INSERT INTO Disease (plant_id, disease_name, cause, treatment, symptom_description, example_image_url, confidence)
VALUES 
(1, 'Apple Scab', 'Fungal', 'Fungicide', 'Dark spots on leaves', 'https://example.com/scab.jpg', 95),
(1, 'Black Rot', 'Fungal', 'Pruning', 'Black cankers', 'https://example.com/black_rot.jpg', 87),
(2, 'Early Blight', 'Fungal', 'Spray', 'Brown spots', 'https://example.com/blight.jpg', 92);

-- Test Predictions
INSERT INTO Prediction (user_id, prediction_type, confidence, uploaded_image_url, created_at, is_valid)
VALUES 
(1, 'identify', 0.95, '/uploads/img1.jpg', GETDATE(), 1),
(1, 'disease', 0.87, '/uploads/img2.jpg', GETDATE(), 1),
(2, 'identify', 0.92, '/uploads/img3.jpg', GETDATE(), 1);

-- Test Prediction_plant links
INSERT INTO Prediction_plant (prediction_id, plant_id)
VALUES 
(1, 1),
(2, 1),
(3, 2);

-- Test Prediction_disease links
INSERT INTO Prediction_disease (prediction_id, disease_id, is_healthy)
VALUES 
(1, 1, 0),
(2, 2, 0),
(3, 3, 0);

-- Check data
SELECT 'Toplam Kullanıcı: ' + CAST(COUNT(*) as VARCHAR) FROM [User];
SELECT 'Toplam Bitki: ' + CAST(COUNT(*) as VARCHAR) FROM Plant;
SELECT 'Toplam Tahmin: ' + CAST(COUNT(*) as VARCHAR) FROM Prediction;
