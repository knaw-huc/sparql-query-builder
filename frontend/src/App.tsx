import React, {Suspense} from 'react';
import './styles/App.scss';
import {Topbar} from './features/topbar/Topbar';
import {Results} from './features/results/Results';
import {QueryBuilder} from './features/querybuilder/QueryBuilder';
import {Notifications} from './features/notifications/Notifications';
import Spinner from 'react-bootstrap/Spinner';
import {LayoutGroup} from 'framer-motion';

const FullScreenLoader = () => 
  <div style={{
    height: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  }}>
    <Spinner animation="border" variant="primary" />
  </div>

const App = () =>
  <Suspense fallback={<FullScreenLoader/>}>
    <div className="App">
      <Topbar />
      <LayoutGroup>
        <QueryBuilder />
        <Results />
      </LayoutGroup>
      <Notifications />
    </div>
  </Suspense>

export default App;
