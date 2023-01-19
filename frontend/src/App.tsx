import React from 'react';
import './styles/App.scss';
import {Topbar} from './features/topbar/Topbar';
import {Results} from './features/results/Results';
import {QueryBuilder} from './features/querybuilder/QueryBuilder';
import {Notifications} from './features/notifications/Notifications';

function App() {
  return (
    <div className="App">
      <Topbar />
      <QueryBuilder />
      <Results />
      <Notifications />
    </div>
  );
}

export default App;
