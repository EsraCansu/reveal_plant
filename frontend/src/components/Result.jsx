/**
 * Tahmin Sonuç Komponenti
 * Tahmin sonuçlarını göster
 */

import React from 'react';
import { Card, Row, Col, Statistic, Progress, Empty } from 'antd';
import { CheckCircleOutlined } from '@ant-design/icons';
import '../styles/result.css';

const ResultComponent = ({ prediction }) => {
  if (!prediction) {
    return <Empty description="Henüz tahmin yapılmadı" />;
  }

  const { top_prediction, all_predictions, processing_time } = prediction;

  return (
    <div>
      {/* Top Prediction */}
      <Card className="result-card" title="🎯 Tahmin Sonucu">
        <Row gutter={[16, 16]}>
          <Col xs={24} md={12}>
            <Statistic
              title="Tahmin Edilen Sınıf"
              value={top_prediction.class_name.replace(/_/g, ' ')}
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
            />
          </Col>
          <Col xs={24} md={12}>
            <Statistic
              title="Güven Oranı"
              value={top_prediction.confidence_percent}
              suffix="%"
              precision={2}
            />
          </Col>
        </Row>

        <Progress
          percent={top_prediction.confidence_percent}
          status={
            top_prediction.confidence_percent > 80
              ? 'success'
              : top_prediction.confidence_percent > 50
              ? 'normal'
              : 'exception'
          }
          style={{ marginTop: '16px' }}
        />

        <p style={{ marginTop: '16px', color: '#666' }}>
          İşlem Süresi: {processing_time.toFixed(3)}s
        </p>
      </Card>

      {/* All Predictions */}
      <Card title="📊 Tüm Tahminler (Top 5)" style={{ marginTop: '16px' }}>
        {all_predictions.map((pred, idx) => (
          <div key={idx} style={{ marginBottom: '16px' }}>
            <Row gutter={16}>
              <Col xs={24} md={12}>
                <p style={{ marginBottom: '4px' }}>
                  <strong>{idx + 1}. {pred.class_name.replace(/_/g, ' ')}</strong>
                </p>
              </Col>
              <Col xs={24} md={12}>
                <p style={{ marginBottom: '4px', textAlign: 'right' }}>
                  {pred.confidence_percent.toFixed(2)}%
                </p>
              </Col>
            </Row>
            <Progress
              percent={pred.confidence_percent}
              strokeColor={
                idx === 0 ? '#52c41a' : idx === 1 ? '#faad14' : '#d9d9d9'
              }
            />
          </div>
        ))}
      </Card>
    </div>
  );
};

export default ResultComponent;
