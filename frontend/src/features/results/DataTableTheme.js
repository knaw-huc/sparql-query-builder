import { createTheme } from 'react-data-table-component';

// Creates a new theme named huc that overrides the build in light theme
createTheme('huc', {
  text: {
    primary: '#3e3e3e',
    secondary: '#3e3e3e',
  },
  background: {
    default: '#f2f2f2',
  },
  context: {
    background: '#cb4b16',
    text: '#3e3e3e',
  },
  divider: {
    default: '#dee2e6',
  },
  highlightOnHover: {
    default: '#3e3e3e',
    text: '#ffffff'
  },
}, 'light');