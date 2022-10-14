import {createTheme} from 'react-data-table-component';

// Creates a new theme named huc that overrides the build in light theme
createTheme('huc', {
  text: {
    primary: '#3e3e3e',
    secondary: '#3e3e3e',
  },
  background: {
    default: '#f2f2f2',
  },
  divider: {
    default: '#e9e9e9',
  },
  highlightOnHover: {
    default: '#e1e1e1',
  },
}, 'light');