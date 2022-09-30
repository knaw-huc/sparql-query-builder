import Container from 'react-bootstrap/Container';
import Dropdown from 'react-bootstrap/Dropdown';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import styles from './QueryBar.module.scss';

// Functionality: toggle the left side panel from builder to raw query code

export function QueryBar() {
  return (
    <Container className={styles.buttonBar}>
      <Row>
        <Col className={styles.rightButtons}>
          <Button variant="light" className={styles.queryButton}>Save Query</Button>
          <Dropdown>
            <Dropdown.Toggle variant="light" className={styles.dropdownButton}>
              Load Query
            </Dropdown.Toggle>
            <Dropdown.Menu>
              <Dropdown.Item href="#">Query #1</Dropdown.Item>
              <Dropdown.Item href="#">Query #2</Dropdown.Item>
              <Dropdown.Item href="#">Query #3</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </Col>
      </Row>
    </Container>
  );
}
