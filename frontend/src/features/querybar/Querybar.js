import Container from 'react-bootstrap/Container';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import styles from './Querybar.module.scss';

// Functionality: toggle the left side panel from builder to raw query code

export function Querybar() {
  return (
    <Container className={styles.buttonBar}>
      <ButtonGroup>
        <Button variant="primary" className={styles.button} active>Query Builder</Button>
        <Button variant="light" className={styles.button}>Sparkl Code Editor</Button>
      </ButtonGroup>
    </Container>
  );
}
