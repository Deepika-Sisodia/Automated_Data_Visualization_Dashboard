import React from 'react';
import { useStore } from '../store';

function FilterSidebar() {
  const { columns, filters, setFilter, clearFilters, reset } = useStore();

  function toggleCheck(col, val, checked) {
    const cur = filters[col] || [];
    setFilter(col, checked ? [...cur, val] : cur.filter(v => v !== val));
  }

  function renderFilter(col) {
    const val = filters[col.name];

    if (col.type === 'CATEGORICAL' && col.uniqueValues) {
      return (
        <div className="filter-checks">
          {col.uniqueValues.map(v => (
            <label key={v} className="filter-check-item">
              <input type="checkbox" checked={(val || []).includes(v)} onChange={e => toggleCheck(col.name, v, e.target.checked)} />
              <span>{v}</span>
            </label>
          ))}
        </div>
      );
    }

    if (col.type === 'NUMERICAL') {
      return (
        <div>
          <div className="filter-range-labels"><span>Min</span><span>Max</span></div>
          <div className="filter-range">
            <input type="number" className="filter-input" placeholder="Min" value={val?.min || ''} onChange={e => setFilter(col.name, { ...val, min: e.target.value })} />
            <input type="number" className="filter-input" placeholder="Max" value={val?.max || ''} onChange={e => setFilter(col.name, { ...val, max: e.target.value })} />
          </div>
        </div>
      );
    }

    if (col.type === 'TEMPORAL') {
      return (
        <div>
          <div className="filter-range-labels"><span>From</span><span>To</span></div>
          <div className="filter-range">
            <input type="date" className="filter-input" value={val?.start || ''} onChange={e => setFilter(col.name, { ...val, start: e.target.value })} />
            <input type="date" className="filter-input" value={val?.end || ''} onChange={e => setFilter(col.name, { ...val, end: e.target.value })} />
          </div>
        </div>
      );
    }

    return null;
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <span className="sidebar-title">Filters</span>
        <button className="sidebar-clear" onClick={clearFilters}>Clear all</button>
      </div>
      <div className="sidebar-body">
        {columns.map(col => (
          <div key={col.name} className="filter-group">
            <div className="filter-label">{col.name}</div>
            {renderFilter(col)}
          </div>
        ))}
      </div>
      <div className="sidebar-footer">
        <button className="btn-new-dataset" onClick={reset}>↑ Upload New File</button>
      </div>
    </aside>
  );
}

export default FilterSidebar;
