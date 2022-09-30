import Button from 'react-bootstrap/Button';
import Dropdown from 'react-bootstrap/Dropdown';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import { useCookies } from 'react-cookie';
import styles from './QueryBuilder.module.scss';

export function QueryCookies() {
  const [cookies, setCookie] = useCookies(['querylist']);

  function onSave() {
    // todo: get query from redux store and save to cookie
    // 10 queries max
    const newQuery = 'testquery';
    console.log(`saving cookie with: ${newQuery}`);
    console.log(cookies)

    const newList = !cookies.hasOwnProperty('querylist') ? 
      [] :
      ( cookies.querylist.length < 10 ?
        cookies.querylist :
        cookies.querylist.slice(0, -1)
      ) ;

    setCookie(
      'querylist', 
      [ newQuery, ...newList ],
      { path: '/' }
    );
  }

  function onLoad(query: string) {
    // set selected query in redux store
  }

  return (
    <ButtonGroup>
      <Button variant="light" className={styles.groupButton} onClick={ () => onSave() }>Save Query</Button>
      <Dropdown as={ButtonGroup}>
        <Dropdown.Toggle variant="light">
          Load Query
        </Dropdown.Toggle>
        <Dropdown.Menu>
          { cookies.hasOwnProperty('querylist') ?
            cookies.querylist.map( (query: string, i: number) => 
              <Dropdown.Item href="#" key={`query-${i}`}>Query #{i + 1}</Dropdown.Item>)
            :
            <Dropdown.Item href="#">No saved queries</Dropdown.Item>
          }
        </Dropdown.Menu>
      </Dropdown>
    </ButtonGroup>
  );
}
