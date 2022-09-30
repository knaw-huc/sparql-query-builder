import Tab from 'react-bootstrap/Tab';
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
  return (
    <div>
      <Tabs defaultActiveKey="querybuilder">
        <Tab eventKey="querybuilder" title="Query Builder">
          <Builder />
        </Tab>
        <Tab eventKey="editor" title="Sparkl Code Editor">
          <Editor />
        </Tab>
      </Tabs>
      <ButtonToolbar  className={styles.buttonBar}>
        <ButtonGroup>
          <Button variant="primary" size="lg">Run Query</Button>
        </ButtonGroup>
        <QueryCookies />
      </ButtonToolbar>
    </div>
  );
}
