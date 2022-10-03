import React, { useState } from 'react';
import Tab from 'react-bootstrap/Tab';
import Nav from 'react-bootstrap/Nav';
import Tabs from 'react-bootstrap/Tabs';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Dropdown from 'react-bootstrap/Dropdown';
import styles from './QueryBuilder.module.scss';
import { Builder } from './Builder';
import { Editor } from './Editor';
import { QueryCookies } from './QueryCookies';

export function QueryBuilder() {
  const [key, setKey] = useState('querybuilder');

  return (
    <div>
      <Tab.Container 
        activeKey={key}
        onSelect={ (k) => setKey(k!) }>
        <Nav variant="tabs" className={styles.nav} >
          <Nav.Item>
            <Nav.Link 
              eventKey="querybuilder" 
              className={key === 'querybuilder' ? styles.activeItem : styles.item}>
              Query Builder
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link 
              eventKey="editor" 
              className={key === 'editor' ? styles.activeItem : styles.item}>
              Sparql Code Editor
            </Nav.Link>
          </Nav.Item>
        </Nav>
        <Tab.Content className={styles.tabContent}>
          <Tab.Pane eventKey="querybuilder" className={styles.pane}>
            <Builder />
          </Tab.Pane>
          <Tab.Pane eventKey="editor" className={styles.pane}>
            <Editor />
          </Tab.Pane>
        </Tab.Content>
      </Tab.Container>
      <ButtonToolbar className={styles.buttonBar}>
        <ButtonGroup>
          <Button variant="primary" size="lg">Run Query</Button>
        </ButtonGroup>
        <QueryCookies />
      </ButtonToolbar>
    </div>
  );
}
