import React, { useState } from 'react';
import Nav from 'react-bootstrap/Nav';
import Tab from 'react-bootstrap/Tab';
import styles from './TabsPanel.module.scss';
import { Results } from './Results';
import { Datasets } from './Datasets';

export function TabsPanel() {
  const [key, setKey] = useState('results');

  return (
    <Tab.Container 
      activeKey={key}
      onSelect={ (k) => setKey(k!) }>
      <Nav variant="tabs" className={styles.nav} >
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
