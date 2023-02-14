import React, {useState} from 'react';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import Tab from 'react-bootstrap/Tab';
import Nav from 'react-bootstrap/Nav';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import styles from './QueryBuilder.module.scss';
import {Builder} from './components/Builder';
import {Editor} from './components/Editor';
import {QueryCookies} from './components/QueryCookies';
import {Datasets} from '../datasets/Datasets';
import {selectActiveQuery, selectSentQuery, setSentQuery} from './queryBuilderSlice';
import {addNotification} from '../notifications/notificationsSlice';
import {selectSelectedDatasets, selectSentDatasets, setSentDatasets} from '../datasets/datasetsSlice';
import {useTranslation} from 'react-i18next';
import {useRefetchErroredQueryMutation, selectCurrentResults} from '../sparql/sparqlApi';

// TODO: make query firing consistent. Now whn loading a saved query, it gets executed right away if old results are already shown

export function QueryBuilder() {
  const [key, setKey] = useState('querybuilder');
  const currentQuery = useAppSelector(selectActiveQuery);
  const sentQuery = useAppSelector(selectSentQuery);
  const currentDatasets = useAppSelector(selectSelectedDatasets);
  const sentDatasets = useAppSelector(selectSentDatasets);
  const dispatch = useAppDispatch();
  const dataSetEnabled = typeof process.env.REACT_APP_DATASETS_API !== 'undefined';
  const {t} = useTranslation(['querybuilder']);
  const [refetch] = useRefetchErroredQueryMutation();

  const currentQueryState = selectCurrentResults.useQueryState({
    query: currentQuery, 
    datasets: currentDatasets
  });

  function sendQuery() {
    if (
      ((currentQuery === sentQuery || !currentQuery) ||
      (currentQuery === sentQuery && currentDatasets === sentDatasets)) &&
      !currentQueryState.isError
      ) {
      dispatch(addNotification({
        message: !currentQuery ? t('queryBuilder.createQueryWarning') : t('queryBuilder.resultsShownWarning'),
        type: 'warning',
      }));
    }
    // TODO: Remove this optional check?
    else if (currentDatasets.length === 0 && dataSetEnabled) {
      dispatch(addNotification({
        message: t('queryBuilder.selectDatasetsWarning'),
        type: 'warning',
      }));
    }
    else {
      currentQueryState.isError && refetch();
      dispatch(setSentQuery(currentQuery));
      dataSetEnabled && dispatch(setSentDatasets(currentDatasets));
    }
  }

  return (
    <Container fluid="lg">
      <Row className="justify-content-md-center">
        <Col md={8}>
          <Tab.Container 
            activeKey={key}
            onSelect={(k) => setKey(k!) }>
            <Nav variant="tabs" className={styles.nav} >
              <Nav.Item>
                <Nav.Link 
                  eventKey="querybuilder" 
                  className={key === 'querybuilder' ? styles.activeItem : styles.item}>
                  {t('queryBuilder.tabBuilder')}
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link 
                  eventKey="editor" 
                  className={key === 'editor' ? styles.activeItem : styles.item}>
                  {t('queryBuilder.tabEditor')}
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
              <Button 
                variant="primary"
                size="lg"
                onClick={sendQuery}>
                {t('queryBuilder.sendQuery')}
              </Button>
            </ButtonGroup>
            <QueryCookies setKey={setKey} />
          </ButtonToolbar>
        </Col>
        {dataSetEnabled &&
          <Col md={4}>
            <Datasets />
          </Col>
        }
      </Row>
    </Container>
  );
}
