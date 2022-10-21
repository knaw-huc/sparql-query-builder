import Button from 'react-bootstrap/Button';
import Dropdown from 'react-bootstrap/Dropdown';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import {useCookies} from 'react-cookie';
import styles from './QueryBuilder.module.scss';
import {useAppSelector, useAppDispatch} from '../../app/hooks';
import {selectActiveQuery, setActiveQuery} from './queryBuilderSlice';
import {addNotification} from '../notifications/notificationsSlice';
import moment from 'moment';
import {setSelectedDatasets, selectedDatasets} from '../datasets/datasetsSlice';
import type {Dataset} from '../datasets/datasetsSlice';

// TODO: 
// later: querybuilder in cookie

export interface QueryCookieObject {
  query: string;
  datetime: string;
  datasets: Dataset[];
}

/* 
 * Getter and setter for query cookies.
 * Saves a max of 10 queries in a cookie 'querylist'
 * Save: gets the currently entered query from the Sparql query editor (from redux store) and adds this to the cookie list, 
 * with current datetime and selected data sets
 * Load: gets the value of the selected query, selected data sets, and saves this to the redux store
*/

export function QueryCookies() {
  const [cookies, setCookie] = useCookies(['querylist']);
  const currentQuery = useAppSelector(selectActiveQuery);
  const currentDatasets = useAppSelector(selectedDatasets);
  const dispatch = useAppDispatch();

  function onSave() {
    const newList = !cookies.hasOwnProperty('querylist') ? 
      [] :
      ( cookies.querylist.length < 10 ?
        cookies.querylist :
        cookies.querylist.slice(0, -1)
      );

    if (!currentQuery) {
      dispatch(
        addNotification({
          message: 'Build a query first',
          type: 'warning',
        })
      );
      return;
    }

    if (newList.filter( (q: QueryCookieObject) => q.query === currentQuery).length > 0) {
      dispatch(
        addNotification({
          message: `Query already in list`,
          type: 'warning',
        })
      );
      return;
    }

    setCookie(
      'querylist', 
      [
        {
          query: currentQuery,
          datetime: moment().format('D-M-YYYY H:mm'),
          datasets: currentDatasets,
        }, 
        ...newList
      ],
      {path: '/' }
    );

    dispatch(
      addNotification({
        message: `Query succesfully saved`,
        type: 'info',
      })
    );
  }

  function onLoad(query: QueryCookieObject) {
    dispatch(setActiveQuery(query.query));
    dispatch(setSelectedDatasets(query.datasets));
    dispatch(
      addNotification({
        message: `Query succesfully loaded into the code editor. Click <b>Run Query</b> to execute.`,
        type: 'info',
      })
    );
  }

  return (
    <ButtonGroup>
      <Button 
        variant="secondary" 
        className={styles.groupButton} 
        onClick={() => onSave() }>
        Save Query
      </Button>
      <Dropdown as={ButtonGroup}>
        <Dropdown.Toggle variant="secondary">
          Load Query
        </Dropdown.Toggle>
        <Dropdown.Menu className={styles.loadQuery} variant="secondary">
          {cookies.hasOwnProperty('querylist') ?
            cookies.querylist.map( (query: QueryCookieObject, i: number) => 
              <Dropdown.Item 
                key={`query-${i}`} 
                onClick={() => onLoad(query) }
                className={currentQuery === query.query ? styles.loadQueryItemActive : styles.loadQueryItem}
              >
                <div>
                  <span className={styles.cookieQueryHeader}>Query #{i + 1}</span>
                  <span className={styles.cookieQueryDescription}>Saved on {query.datetime}</span>
                </div>
              </Dropdown.Item>)
            :
            <Dropdown.Item>No saved queries</Dropdown.Item>
          }
        </Dropdown.Menu>
      </Dropdown>
    </ButtonGroup>
  );
}
