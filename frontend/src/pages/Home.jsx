/**
 * Ana Sayfa
 */

import React, { useState } from 'react';
import { Container, Row, Col } from 'react-grid-system';
import { Layout, Button } from 'antd';
import UploadComponent from '../components/Upload';
import ResultComponent from '../components/Result';
import '../styles/home.css';

const { Content } = Layout;

const HomePage = () => {
  const [prediction, setPrediction] = useState(null);

  const handlePredictionComplete = (result) => {
    setPrediction(result);
  };

  const handleReset = () => {
    setPrediction(null);
  };

  return (
    <Content style={{ padding: '50px 20px', minHeight: '80vh' }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <h1 style={{ textAlign: 'center', marginBottom: '40px' }}>
          🌱 Reveal Plant - Bitki Hastalığı Tespiti
        </h1>

        <Row gutter={[32, 32]}>
          <Col md={12} xs={24}>
            <UploadComponent onPredictionComplete={handlePredictionComplete} />
          </Col>

          <Col md={12} xs={24}>
            {prediction ? (
              <>
                <ResultComponent prediction={prediction} />
                <Button
                  type="primary"
                  size="large"
                  block
                  onClick={handleReset}
                  style={{ marginTop: '16px' }}
                >
                  Yeni Tahmin Yap
                </Button>
              </>
            ) : (
              <div
                style={{
                  textAlign: 'center',
                  padding: '40px',
                  background: '#f0f2f5',
                  borderRadius: '8px',
                }}
              >
                <p style={{ color: '#666' }}>
                  Tahmin sonuçları burada görüntülenecek
                </p>
              </div>
            )}
          </Col>
        </Row>
      </div>
    </Content>
  );
};

export default HomePage;
