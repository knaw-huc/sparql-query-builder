import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import styles from './Topbar.module.scss';
import Logo from "../../images/logo-ga.png";

export function Topbar() {
  return (
    <Navbar bg="primary" variant="light" expand="lg" className={styles.topbar}>
      <Navbar.Brand href="/" className={styles.brand}>
        <img 
          src={Logo} 
          className={styles.logo}
          alt="Golden Agents"
          title="Golden Agents" />
          <span className="d-none d-sm-inline">Search the Golden Agents</span>
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" className={styles.toggler}/>
      <Navbar.Collapse id="basic-navbar-nav" className="justify-content-end">
        <Nav className={styles.nav}>
          <NavDropdown title="About" menuVariant="dark" align="end">
            <NavDropdown.Item href="https://ga.sd.di.huc.knaw.nl/">
              About this tool
            </NavDropdown.Item>
            <NavDropdown.Item href="https://ga-wp3.sd.di.huc.knaw.nl/">
              About the Golden Agents project
            </NavDropdown.Item>
            <NavDropdown.Item href="https://www.goldenagents.org/">
              Visit goldenagents.org
            </NavDropdown.Item>
          </NavDropdown>
          <NavDropdown title="More tools" menuVariant="dark" align="end">
            <NavDropdown.Item href="https://ga.sd.di.huc.knaw.nl/">
              Dataset Browser
            </NavDropdown.Item>
            <NavDropdown.Item href="https://ga-wp3.sd.di.huc.knaw.nl/">
              Golden Agent Search
            </NavDropdown.Item>
            <NavDropdown.Item href="https://lenticularlens.org/">
              Lenticular Lens
            </NavDropdown.Item>
            <NavDropdown.Item href="#">
              Analiticcl
            </NavDropdown.Item>
          </NavDropdown>
          <NavDropdown title="Help" menuVariant="dark" align="end">
            <NavDropdown.Item href="#">
              Watch instruction videos
            </NavDropdown.Item>
            <NavDropdown.Item href="#">
              Find a dataset
            </NavDropdown.Item>
            <NavDropdown.Item href="#">
              Documentation
            </NavDropdown.Item>
          </NavDropdown>
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  );
}
