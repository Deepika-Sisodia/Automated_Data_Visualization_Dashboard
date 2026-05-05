import { create } from 'zustand';

export const useStore = create((set) => ({
  datasetId: null,
  columns: [],
  preview: [],
  filters: {},

  setDatasetData: (data) => set({
    datasetId: data.datasetId,
    columns: data.columns,
    preview: data.preview,
    filters: {},
  }),

  setFilter: (name, value) => set((s) => ({
    filters: { ...s.filters, [name]: value },
  })),

  clearFilters: () => set({ filters: {} }),
  reset: () => set({ datasetId: null, columns: [], preview: [], filters: {} }),
}));
