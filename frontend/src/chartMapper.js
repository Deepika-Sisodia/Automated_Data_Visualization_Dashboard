export function generateChartConfigs(columns) {
  const configs = [];
  const temporals = columns.filter(c => c.type === 'TEMPORAL');
  const categoricals = columns.filter(c => c.type === 'CATEGORICAL');
  const numerics = columns.filter(c => c.type === 'NUMERICAL');

  if (temporals.length > 0 && numerics.length > 0) {
    configs.push({
      id: 'line_main',
      type: 'LineChart',
      xAxis: temporals[0].name,
      yAxis: numerics[0].name,
      title: `${numerics[0].name} over ${temporals[0].name}`
    });
  }

  if (categoricals.length > 0 && numerics.length > 0) {
    configs.push({
      id: 'pie_main',
      type: 'PieChart',
      xAxis: categoricals[0].name,
      yAxis: numerics[0].name,
      title: `${numerics[0].name} by ${categoricals[0].name}`
    });
  }

  if (categoricals.length > 0 && numerics.length > 0) {
    const numIdx = numerics.length > 1 ? 1 : 0;
    const catIdx = categoricals.length > 1 ? 1 : 0;
    configs.push({
      id: 'bar_main',
      type: 'BarChart',
      xAxis: categoricals[catIdx].name,
      yAxis: numerics[numIdx].name,
      title: `${numerics[numIdx].name} by ${categoricals[catIdx].name}`
    });
  }

  if (configs.length === 0 && numerics.length > 0) {
    configs.push({
      id: 'bar_fallback',
      type: 'BarChart',
      xAxis: null,
      yAxis: numerics[0].name,
      title: `${numerics[0].name} Distribution`
    });
  }

  return configs;
}
