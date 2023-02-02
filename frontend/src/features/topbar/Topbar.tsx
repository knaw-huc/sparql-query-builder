import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import styles from './Topbar.module.scss';
import Logo from "../../images/logo-ga.png";
import {useTranslation} from 'react-i18next';

export const Topbar = () => {
  const {t} = useTranslation(['topbar']);
  return (
    <Navbar bg="primary" variant="light" expand="lg" className={styles.topbar}>
      <Navbar.Brand href="/" className={styles.brand}>
        <img 
          src={Logo} 
          className={styles.logo}
          alt={t('title') as string}
          title={t('title') as string} />
          <span className="d-none d-sm-inline">{t('header')}</span>
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" className={styles.toggler}/>
      <Navbar.Collapse id="basic-navbar-nav" className="justify-content-end">
        <Nav className={styles.nav}>
          <NavDropdown title={t('about.title') as string} menuVariant="dark" align="end">
            <NavDropdown.Item href="https://ga.sd.di.huc.knaw.nl/">
              {t('about.dropdown.item1')}
            </NavDropdown.Item>
            <NavDropdown.Item href="https://ga-wp3.sd.di.huc.knaw.nl/">
              {t('about.dropdown.item2')}
            </NavDropdown.Item>
            <NavDropdown.Item href="https://www.goldenagents.org/">
              {t('about.dropdown.item3')}      
            </NavDropdown.Item>
          </NavDropdown>
          <NavDropdown title={t('tools.title') as string} menuVariant="dark" align="end">
            <NavDropdown.Item href="https://ga.sd.di.huc.knaw.nl/">
              {t('tools.dropdown.item1')}
            </NavDropdown.Item>
            <NavDropdown.Item href="https://ga-wp3.sd.di.huc.knaw.nl/">
              {t('tools.dropdown.item2')}
            </NavDropdown.Item>
            <NavDropdown.Item href="https://lenticularlens.org/">
              {t('tools.dropdown.item3')}
            </NavDropdown.Item>
            <NavDropdown.Item href="#">
              {t('tools.dropdown.item4')}
            </NavDropdown.Item>
          </NavDropdown>
          <NavDropdown title={t('help.title') as string} menuVariant="dark" align="end">
            <NavDropdown.Item href="#">
              {t('help.dropdown.item1')}
            </NavDropdown.Item>
            <NavDropdown.Item href="#">
              {t('help.dropdown.item2')}
            </NavDropdown.Item>
            <NavDropdown.Item href="#">
              {t('help.dropdown.item3')}
            </NavDropdown.Item>
          </NavDropdown>
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  )
}
