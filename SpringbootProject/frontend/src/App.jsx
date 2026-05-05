import React from 'react';
import { useStore } from './store';
import UploadScreen from './components/UploadScreen';
import Dashboard from './components/Dashboard';

function App() {
  const datasetId = useStore(s => s.datasetId);
  return datasetId ? <Dashboard /> : <UploadScreen />;
}

export default App;
