import Button from 'react-bootstrap/Button';
import Dropdown from 'react-bootstrap/Dropdown';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import {useCookies} from 'react-cookie';
import styles from './QueryCookies.module.scss';
import {useAppSelector, useAppDispatch} from '../../../app/hooks';
import {
  selectActiveQuery, 
  setActiveQuery, 
  selectSelectedEntity, 
  // setSelectedEntity, 
  // selectSelectedProperties,
  // setSelectedProperties, 
  // selectSelectedLimit
  // setSelectedLimit,
} from '../queryBuilderSlice';
import type {QueryCookiesFn, QueryCookieObject} from '../../../types/queryBuilder';
import {addNotification} from '../../notifications/notificationsSlice';
import moment from 'moment';
import {setSelectedDatasets, selectSelectedDatasets} from '../../datasets/datasetsSlice';
import {useTranslation} from 'react-i18next';
import {v4 as uuidv4} from 'uuid';

/* 
 * Getter and setter for query cookies.
 * Saves a max of 10 queries in a range of cookies 'querylist-uuid'
 * Save: gets the currently entered query from the Sparql query editor (from redux store) and adds this to the cookie list, 
 * with current datetime and selected data sets
 * Load: gets the value of the selected query, selected data sets, and saves this to the redux store
*/

export function QueryCookies({setKey}: QueryCookiesFn) {
  const [cookies, setCookie, removeCookie] = useCookies();
  const currentQuery = useAppSelector(selectActiveQuery);
  const currentDatasets = useAppSelector(selectSelectedDatasets);
  const selectedEntity = useAppSelector(selectSelectedEntity);
  /*
   * Cookies are limited to 4 KB in size, so we cannot really save the QB for larger queries. 
   * Otherwise, uncomment these lines, as well as those in setCookie and onLoad and the QueryCookieObject interface
  */
  // const selectedProperties = useAppSelector(selectSelectedProperties);
  // const selectedLimit = useAppSelector(selectSelectedLimit);
  const dispatch = useAppDispatch();
  const {t} = useTranslation(['querybuilder']);

  // Just in case we'll save 1 query to 1 cookie, as 10 big queries won't fit
  // Reverse the list to have newest query at the top
  const cookieList = Object.keys(cookies)
    .filter((c: string) => c.indexOf('querylist') !== -1)
    .reduce((obj: QueryCookieObject[], key: string) => [...obj, cookies[key]], [])
    .reverse();

  function onSave() {
    const uuid = uuidv4();
    const now = new Date();
    const newDate = new Date(now.getTime() + 86400000 * 14);

    if (!currentQuery) {
      dispatch(
        addNotification({
          message: t('queryCookies.buildWarning'),
          type: 'warning',
        })
      );
      return;
    }

    if (cookieList.filter((q: QueryCookieObject) => q.query === currentQuery).length > 0) {
      dispatch(
        addNotification({
          message: t('queryCookies.existsWarning'),
          type: 'warning',
        })
      );
      return;
    }

    // Set a max of 10 cookies, remove oldest query
    if(cookieList.length > 9) {
      removeCookie(`querylist-${cookieList[cookieList.length-1].uuid}`);
    }

    setCookie(
      `querylist-${uuid}`, 
      {
        query: currentQuery,
        datetime: moment().format('D-M-YYYY H:mm'),
        datasets: currentDatasets,
        uuid: uuid,
        entity: selectedEntity,
        // properties: selectedProperties,
        // limit: selectedLimit,
      },
      {
        path: '/',
        expires: newDate,
        sameSite: 'lax'
      }
    );

    dispatch(
      addNotification({
        message: t('queryCookies.saved'),
        type: 'info',
      })
    );
  }

  function onLoad(query: QueryCookieObject) {
    // Load the query
    dispatch(setActiveQuery(query.query));
    dispatch(setSelectedDatasets(query.datasets));
    // dispatch(setSelectedEntity(query.entity));
    // dispatch(setSelectedProperties(query.properties));
    // dispatch(setSelectedLimit(query.limit));
    dispatch(
      addNotification({
        message: t('queryCookies.loaded'),
        type: 'info',
      })
    );
    // Change tab view to editor, since we're not loading the QB
    setKey('editor');
  }

  return (
    <ButtonGroup>
      <Button 
        variant="secondary" 
        className={styles.groupButton} 
        onClick={() => onSave() }>
        {t('queryCookies.save')}
      </Button>
      <Dropdown as={ButtonGroup}>
        <Dropdown.Toggle variant="secondary">
          {t('queryCookies.load')}
        </Dropdown.Toggle>
        <Dropdown.Menu className={styles.loadQuery} variant="secondary">
          {cookieList.length > 0 ?
            cookieList.map((query: QueryCookieObject, i: number) => 
              <Dropdown.Item 
                key={`query-${i}`} 
                onClick={() => onLoad(query) }
                className={currentQuery === query.query && currentDatasets === query.datasets ? styles.loadQueryItemActive : styles.loadQueryItem}
              >
                <div>
                  <span className={styles.cookieQueryHeader}>{t('queryCookies.entity', {entity: query.entity.label})}</span>
                  <span className={styles.cookieQueryDescription}>{t('queryCookies.savedOn', {datetime: query.datetime})}</span>
                </div>
              </Dropdown.Item>
            )
            :
            <Dropdown.Item>{t('queryCookies.empty')}</Dropdown.Item>
          }
        </Dropdown.Menu>
      </Dropdown>
    </ButtonGroup>
  );
}
