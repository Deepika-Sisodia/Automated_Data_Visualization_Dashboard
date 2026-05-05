import React, { useEffect, useState, useRef, useMemo } from 'react';
import { useStore } from '../store';
import FilterSidebar from './FilterSidebar';
import { generateChartConfigs } from '../chartMapper';
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, Cell
} from 'recharts';
import html2canvas from 'html2canvas';

const COLORS = ['#E74C3C', '#3498DB', '#2ECC71', '#F39C12', '#9B59B6', '#1ABC9C', '#E67E22', '#34495E'];
const TIP = { borderRadius: '8px', border: '1px solid #E8DFD3', boxShadow: '0 4px 12px rgba(0,0,0,0.08)', fontSize: '12px', fontFamily: 'DM Sans' };
const TICK = { fontSize: 11, fill: '#7A6652' };
const MARGIN = { top: 10, right: 20, left: 0, bottom: 0 };

function ChartCard({ config, data }) {
  const ref = useRef(null);

  async function exportPng() {
    if (!ref.current) return;
    const canvas = await html2canvas(ref.current, { backgroundColor: '#FFF' });
    const link = document.createElement('a');
    link.download = `${config.title}.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
  }

  function aggregateData() {
    if (!data || data.length === 0) return [];
    if (config.type === 'LineChart') {
      const sorted = [...data].sort((a, b) => String(a[config.xAxis]).localeCompare(String(b[config.xAxis])));
      return sorted;
    }
    if (config.type === 'PieChart' || config.type === 'BarChart') {
      const grouped = {};
      data.forEach(row => {
        const key = String(row[config.xAxis] || 'Unknown');
        if (!grouped[key]) grouped[key] = 0;
        const val = Number(row[config.yAxis]) || 0;
        grouped[key] += val;
      });
      return Object.entries(grouped).map(([name, value]) => ({ name, value: Math.round(value) }));
    }
    return data;
  }

  function chart() {
    const chartData = aggregateData();
    if (chartData.length === 0) return <div className="chart-empty">No data</div>;

    const grid = <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E8DFD3" />;
    const tip = <Tooltip contentStyle={TIP} />;

    if (config.type === 'LineChart') {
      return (
        <LineChart data={chartData} margin={MARGIN}>
          {grid}
          <XAxis dataKey={config.xAxis} tick={TICK} axisLine={false} tickLine={false} />
          <YAxis tick={TICK} axisLine={false} tickLine={false} />
          {tip}
          <Line isAnimationActive={false} type="monotone" dataKey={config.yAxis} stroke="#E74C3C" strokeWidth={2.5} dot={{ fill: '#E74C3C', r: 3 }} activeDot={{ r: 5 }} />
        </LineChart>
      );
    }

    if (config.type === 'BarChart') {
      return (
        <BarChart data={chartData} margin={MARGIN}>
          {grid}
          <XAxis dataKey="name" tick={TICK} axisLine={false} tickLine={false} />
          <YAxis tick={TICK} axisLine={false} tickLine={false} />
          {tip}
          <Bar isAnimationActive={false} dataKey="value" radius={[6, 6, 0, 0]}>
            {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
          </Bar>
        </BarChart>
      );
    }

    if (config.type === 'PieChart') {
      return (
        <PieChart>
          {tip}
          <Legend wrapperStyle={{ fontSize: 11 }} />
          <Pie isAnimationActive={false} data={chartData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={85} innerRadius={30} label={{ fontSize: 11 }}>
            {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
          </Pie>
        </PieChart>
      );
    }

    return null;
  }

  const badges = { LineChart: 'Line', BarChart: 'Bar', PieChart: 'Pie' };

  return (
    <div ref={ref} className="chart-card">
      <div className="chart-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, overflow: 'hidden' }}>
          <span className="chart-title">{config.title}</span>
          <span className="chart-type-badge">{badges[config.type]}</span>
        </div>
        <button className="chart-export-btn" onClick={exportPng}>Export ↓</button>
      </div>
      <div className="chart-body">
        <ResponsiveContainer width="100%" height="100%">{chart()}</ResponsiveContainer>
      </div>
    </div>
  );
}

function DataTable({ datasetId, data, columns, onDataChanged }) {
  const [editRow, setEditRow] = useState(null);
  const [editData, setEditData] = useState({});
  const [addingRow, setAddingRow] = useState(false);
  const [newRow, setNewRow] = useState({});

  function startEdit(index) {
    setEditRow(index);
    setEditData({ ...data[index] });
  }

  function cancelEdit() {
    setEditRow(null);
    setEditData({});
  }

  async function saveEdit(index) {
    const processedData = {};
    for (const [key, val] of Object.entries(editData)) {
      const col = columns.find(c => c.name === key);
      if (col && col.type === 'NUMERICAL' && val !== null && val !== '') {
        processedData[key] = Number(val);
      } else {
        processedData[key] = val;
      }
    }
    await fetch(`/api/dataset/${datasetId}/row/${index}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(processedData)
    });
    setEditRow(null);
    onDataChanged();
  }

  async function deleteRow(index) {
    await fetch(`/api/dataset/${datasetId}/row/${index}`, { method: 'DELETE' });
    onDataChanged();
  }

  async function saveNewRow() {
    const processedData = {};
    for (const [key, val] of Object.entries(newRow)) {
      const col = columns.find(c => c.name === key);
      if (col && col.type === 'NUMERICAL' && val !== null && val !== '') {
        processedData[key] = Number(val);
      } else {
        processedData[key] = val;
      }
    }
    await fetch(`/api/dataset/${datasetId}/row`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(processedData)
    });
    setAddingRow(false);
    setNewRow({});
    onDataChanged();
  }

  const headers = columns.map(c => c.name);

  return (
    <div className="data-table-wrap">
      <div className="data-table-header">
        <h2 className="data-table-title">Data Manager</h2>
        <span className="data-table-count">{data.length} rows</span>
        <button className="btn-add-row" onClick={() => { setAddingRow(true); setNewRow({}); }}>+ Add Row</button>
      </div>
      <div className="data-table-scroll">
        <table className="data-table">
          <thead>
            <tr>
              {headers.map(h => <th key={h}>{h}</th>)}
              <th style={{ width: 120 }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {addingRow && (
              <tr className="editing-row">
                {headers.map(h => (
                  <td key={h}><input className="cell-input" value={newRow[h] || ''} onChange={e => setNewRow({ ...newRow, [h]: e.target.value })} placeholder={h} /></td>
                ))}
                <td>
                  <div className="action-btns">
                    <button className="btn-save" onClick={saveNewRow}>Save</button>
                    <button className="btn-cancel" onClick={() => setAddingRow(false)}>Cancel</button>
                  </div>
                </td>
              </tr>
            )}
            {data.map((row, i) => (
              <tr key={i} className={editRow === i ? 'editing-row' : ''}>
                {headers.map(h => (
                  <td key={h}>
                    {editRow === i
                      ? <input className="cell-input" value={editData[h] ?? ''} onChange={e => setEditData({ ...editData, [h]: e.target.value })} />
                      : <span>{row[h] != null ? String(row[h]) : ''}</span>
                    }
                  </td>
                ))}
                <td>
                  <div className="action-btns">
                    {editRow === i ? (
                      <>
                        <button className="btn-save" onClick={() => saveEdit(i)}>Save</button>
                        <button className="btn-cancel" onClick={cancelEdit}>Cancel</button>
                      </>
                    ) : (
                      <>
                        <button className="btn-edit" onClick={() => startEdit(i)}>Edit</button>
                        <button className="btn-delete" onClick={() => deleteRow(i)}>Delete</button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function Dashboard() {
  const { datasetId, columns, filters } = useStore();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const charts = useMemo(() => generateChartConfigs(columns), [columns]);

  function fetchData() {
    if (!datasetId) return;
    setLoading(true);
    setError(null);
    const params = new URLSearchParams();
    if (filters && Object.keys(filters).length > 0) params.append('filters', JSON.stringify(filters));
    fetch(`/api/dataset/${datasetId}?${params}`)
      .then(r => { if (!r.ok) throw new Error('Failed'); return r.json(); })
      .then(setData)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    const timer = setTimeout(fetchData, 300);
    return () => clearTimeout(timer);
  }, [datasetId, filters]);

  return (
    <div className="dashboard">
      <FilterSidebar />
      <main className="main-content">
        <div className="main-header">
          <h1 className="main-title">Dashboard</h1>
          <div className="main-actions">
            <button className="btn-primary" onClick={() => window.location.href = `/api/export/${datasetId}`}>Export Report</button>
          </div>
        </div>

        {error && <div className="error-toast">{error}</div>}

        {loading ? (
          <div className="skeleton-grid">
            {[1, 2, 3].map(i => <div key={i} className="skeleton-card"><div className="skeleton-bar title" /><div className="skeleton-bar body" /></div>)}
          </div>
        ) : (
          <>
            <div className="chart-grid">
              {charts.map(c => <ChartCard key={c.id} config={c} data={data} />)}
            </div>
            <DataTable datasetId={datasetId} data={data} columns={columns} onDataChanged={fetchData} />
          </>
        )}
      </main>
    </div>
  );
}

export default Dashboard;
