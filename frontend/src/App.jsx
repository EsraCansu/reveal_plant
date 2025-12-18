/**
 * App.js - Main Component
 */

import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import HomePage from './pages/Home';
import './App.css';

const { Header, Content, Footer } = Layout;

function App() {
  return (
    <Router>
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ background: '#001529', padding: '0 20px' }}>
          <h1 style={{ color: 'white', margin: '0', lineHeight: '64px' }}>
            🌱 Reveal Plant
          </h1>
        </Header>

        <Routes>
          <Route path="/" element={<HomePage />} />
        </Routes>

        <Footer style={{ textAlign: 'center', marginTop: 'auto' }}>
          Reveal Plant ©2024 - Bitki Hastalığı Tespiti Sistemi
        </Footer>
      </Layout>
    </Router>
  );
}

export default App;
