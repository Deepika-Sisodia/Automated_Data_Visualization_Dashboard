import React, { useState, useRef } from 'react';
import { useStore } from '../store';

function UploadScreen() {
  const [dragging, setDragging] = useState(false);
  const [error, setError] = useState(null);
  const [uploading, setUploading] = useState(false);
  const fileInput = useRef(null);
  const saveDataset = useStore(s => s.setDatasetData);

  async function uploadFile(file) {
    setError(null);
    if (!file) return;
    if (!file.name.endsWith('.csv') && !file.name.endsWith('.xlsx')) {
      setError('Please upload a .csv or .xlsx file.');
      return;
    }

    setUploading(true);
    const body = new FormData();
    body.append('file', file);

    try {
      const res = await fetch('/api/upload', { method: 'POST', body });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Upload failed');
      saveDataset(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
    }
  }

  function onDrag(e) {
    e.preventDefault();
    e.stopPropagation();
    setDragging(e.type === 'dragenter' || e.type === 'dragover');
  }

  function onDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    setDragging(false);
    if (e.dataTransfer.files?.[0]) uploadFile(e.dataTransfer.files[0]);
  }

  const cls = ['upload-dropzone', dragging && 'dragging', uploading && 'disabled'].filter(Boolean).join(' ');

  return (
    <div className="upload-page">
      <div className="upload-card">
        <h1>AutomateCSV</h1>
        <p className="upload-subtitle">Upload your data, get instant charts</p>

        {error && <div className="upload-error">{error}</div>}

        <div className={cls} onDragEnter={onDrag} onDragLeave={onDrag} onDragOver={onDrag} onDrop={onDrop} onClick={() => fileInput.current.click()}>
          <input ref={fileInput} type="file" accept=".csv,.xlsx" style={{ display: 'none' }} onChange={(e) => e.target.files?.[0] && uploadFile(e.target.files[0])} />
          {uploading ? (
            <div className="upload-spinner">
              <div className="spinner" />
              <span className="upload-dropzone-hint">Analyzing…</span>
            </div>
          ) : (
            <>
              <svg className="upload-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
              <span className="upload-dropzone-title">Click or drop file here</span>
              <span className="upload-dropzone-hint">Supports .csv and .xlsx</span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default UploadScreen;
