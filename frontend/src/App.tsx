import React, {Suspense} from 'react';
import './styles/App.scss';
import {Topbar} from './features/topbar/Topbar';
import {Results} from './features/results/Results';
import {QueryBuilder} from './features/querybuilder/QueryBuilder';
import {Notifications} from './features/notifications/Notifications';
import Spinner from 'react-bootstrap/Spinner';
import {LayoutGroup} from 'framer-motion';
import { MatchMediaBreakpoints } from 'react-hook-breakpoints';

const breakpoints = {
  small: 576,
  medium: 768,
  large: 992,
  xlarge: 1200,
  xxlarge: 1400,
};


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
  <MatchMediaBreakpoints breakpoints={breakpoints}>
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
  </MatchMediaBreakpoints>

export default App;
