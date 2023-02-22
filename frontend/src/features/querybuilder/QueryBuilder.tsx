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
import {LayoutMotionDiv} from '../animations/Animations';

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
    const dataSetDifference = dataSetEnabled && (
      currentDatasets.length !== sentDatasets.length || 
      currentDatasets.filter(({ id: id1 }) => !sentDatasets.some(({ id: id2 }) => id2 === id1)).length > 0
    );

    if (
      !currentQueryState.isError &&
      (
        (!dataSetEnabled && (currentQuery === sentQuery || !currentQuery)) || 
        (dataSetEnabled && ((!dataSetDifference || sentDatasets.length === 0) && (currentQuery === sentQuery || !currentQuery)))
      )
    ) {
      dispatch(addNotification({
        message: !currentQuery ? 
          t('queryBuilder.createQueryWarning') : 
          currentQueryState.isFetching ?
          t('queryBuilder.stillFetching') :
          t('queryBuilder.resultsShownWarning'),
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
          <LayoutMotionDiv>
            <Tab.Container 
              activeKey={key}
              onSelect={(k) => setKey(k!)}>
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
                <Tab.Pane key="qb" eventKey="querybuilder" className={styles.pane}>
                  <Builder />
                </Tab.Pane>
                <Tab.Pane key="ed" eventKey="editor" className={styles.pane}>
                  <Editor />
                </Tab.Pane>
              </Tab.Content>
            </Tab.Container>
          </LayoutMotionDiv>
          <LayoutMotionDiv>
            <ButtonToolbar className={styles.buttonBar}>
              <ButtonGroup>
                <Button 
                  variant="primary"
                  onClick={sendQuery}>
                  {t('queryBuilder.sendQuery')}
                </Button>
              </ButtonGroup>
              <QueryCookies setKey={setKey} />
            </ButtonToolbar>
          </LayoutMotionDiv>
        </Col>
        {dataSetEnabled &&
          <Col md={4}>
            <LayoutMotionDiv>
              <Datasets />
            </LayoutMotionDiv>
          </Col>
        }
      </Row>
    </Container>
  );
}
