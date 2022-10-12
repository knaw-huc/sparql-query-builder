import React from 'react';

import './styles/App.scss';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import { Topbar } from './features/topbar/Topbar';
import { Results } from './features/results/Results';
import { QueryBuilder } from './features/querybuilder/QueryBuilder';
import { Notifications } from './features/notifications/Notifications';

function App() {
  return (
    <div className="App">
      <Topbar />
      {/*<QueryBar />*/}
      <QueryBuilder />
      <Results />
      <Notifications />
    </div>
  );
}

export default App;
