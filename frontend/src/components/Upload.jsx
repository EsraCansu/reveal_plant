/**
 * Upload Komponenti
 * Görsel seçme ve yükleme
 */

import React, { useState } from 'react';
import { Upload, Button, Card, Spin, Alert, Row, Col } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import { predictionService } from '../services/predictionService';
import '../styles/upload.css';

const UploadComponent = ({ onPredictionComplete }) => {
  const [loading, setLoading] = useState(false);
  const [fileList, setFileList] = useState([]);
  const [error, setError] = useState(null);
  const [preview, setPreview] = useState(null);

  const handleUpload = async (file) => {
    setLoading(true);
    setError(null);

    try {
      const response = await predictionService.getPrediction(file);
      
      if (response.success) {
        // Preview oluştur
        const reader = new FileReader();
        reader.onload = (e) => {
          setPreview(e.target.result);
        };
        reader.readAsDataURL(file);

        // Parent'a sonucu gönder
        onPredictionComplete(response);
      } else {
        setError('Tahmin başarısız oldu');
      }
    } catch (err) {
      setError(err.message || 'Bir hata oluştu');
    } finally {
      setLoading(false);
    }
  };

  const uploadProps = {
    name: 'file',
    multiple: false,
    accept: 'image/*',
    maxCount: 1,
    onChange: (info) => {
      setFileList(info.fileList);
    },
    beforeUpload: (file) => {
      handleUpload(file);
      return false;
    },
  };

  return (
    <Card className="upload-card">
      <Spin spinning={loading} tip="Tahmin yapılıyor...">
        <Row gutter={[16, 16]}>
          <Col xs={24} md={12}>
            <Upload.Dragger {...uploadProps}>
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">
                Görselinizi buraya sürükleyin veya tıklayın
              </p>
              <p className="ant-upload-hint">
                JPG, PNG formatında dosya yükleyebilirsiniz
              </p>
            </Upload.Dragger>
          </Col>

          {preview && (
            <Col xs={24} md={12}>
              <div className="preview-container">
                <h3>Önizleme</h3>
                <img src={preview} alt="Preview" className="preview-image" />
              </div>
            </Col>
          )}
        </Row>

        {error && (
          <Alert
            message="Hata"
            description={error}
            type="error"
            closable
            style={{ marginTop: '16px' }}
          />
        )}
      </Spin>
    </Card>
  );
};

export default UploadComponent;
