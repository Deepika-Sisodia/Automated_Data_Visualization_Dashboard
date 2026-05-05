import React from 'react';
import { useStore } from './store';
import UploadScreen from './components/UploadScreen';
import Dashboard from './components/Dashboard';

function App() {
  const datasetId = useStore(s => s.datasetId);
  return (
    <div className="app-container">
      <nav className="navbar">
        <img src="/logo.png" alt="AutomateCSV Logo" className="navbar-logo" />
        <span className="navbar-title">AutomateCSV</span>
      </nav>
      <div className="app-content">
        {datasetId ? <Dashboard /> : <UploadScreen />}
      </div>
    </div>
  );
}

export default App;
