import React from 'react';

import './styles/App.scss';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import { Topbar } from './features/topbar/Topbar';
import { TabsPanel } from './features/tabs/TabsPanel';
import { QueryBuilder } from './features/querybuilder/QueryBuilder';
import { Notifications } from './features/notifications/Notifications';

function App() {
  return (
    <div className="App">
      <Topbar />
      {/*<QueryBar />*/}
      <Container fluid="lg">
        <Row>
          <Col lg={6}>
            <QueryBuilder />
          </Col>
          <Col lg={6} className="right-column">
            <TabsPanel />
          </Col>
        </Row>
      </Container>
      <Notifications />
    </div>
  );
}

export default App;
