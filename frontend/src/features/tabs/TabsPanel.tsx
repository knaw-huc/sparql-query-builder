import React, { useState } from 'react';
import Col from 'react-bootstrap/Col';
import Nav from 'react-bootstrap/Nav';
import Row from 'react-bootstrap/Row';
import Tab from 'react-bootstrap/Tab';
import styles from './TabsPanel.module.scss';
import { Progress } from './Progress';
import { Results } from './Results';
import { Datasets } from './Datasets';

export function TabsPanel() {
  const [key, setKey] = useState('progress');

  return (
    <Tab.Container 
      activeKey={key}
      onSelect={ (k) => setKey(k!) }>
      <Nav variant="tabs" className={styles.nav} >
        <Nav.Item>
          <Nav.Link 
            eventKey="progress" 
            className={key === 'progress' ? styles.activeItem : styles.item}>
            Progress
          </Nav.Link>
        </Nav.Item>
        <Nav.Item>
          <Nav.Link 
            eventKey="results" 
            className={key === 'results' ? styles.activeItem : styles.item}>
            Results
          </Nav.Link>
        </Nav.Item>
        <Nav.Item>
          <Nav.Link 
            eventKey="datasets"
            className={key === 'datasets' ? styles.activeItem : styles.item}>
            Data sets
          </Nav.Link>
        </Nav.Item>
      </Nav>
      <Tab.Content className={styles.tabContent}>
        <Tab.Pane eventKey="progress" className={styles.pane}>
          <Progress />
        </Tab.Pane>
        <Tab.Pane eventKey="results" className={styles.pane}>
          <Results />
        </Tab.Pane>
        <Tab.Pane eventKey="datasets" className={styles.pane}>
          <Datasets />
        </Tab.Pane>
      </Tab.Content>
    </Tab.Container>
  );
}
