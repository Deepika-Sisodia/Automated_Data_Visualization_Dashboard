import React from 'react';
import { useStore } from './store';


function App() {
  const datasetId = useStore(s => s.datasetId);
  return datasetId ? <Dashboard /> : <UploadScreen />;
}

export default App;
