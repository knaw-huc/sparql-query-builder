import {useState, forwardRef} from 'react';
import type {Ref} from 'react';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import Dropdown, {DropdownProps} from 'react-bootstrap/Dropdown';
import styles from './Topbar.module.scss';
import Logo from "../../images/logo-ga.png";
import {useTranslation} from 'react-i18next';
import AngleDown from '../../images/angle-down-solid.svg';
import Collapse from 'react-bootstrap/Collapse';

export const Topbar = () => {
  const {t} = useTranslation(['topbar']);
  const [expanded, setExpanded] = useState(false);

  return (
    <Navbar 
      bg="primary" 
      variant="light" 
      expand="lg" 
      className={`${styles.topbar} ${expanded ? styles.topbarExpanded : ''}`} 
      onToggle={() => setExpanded(!expanded)} 
      expanded={expanded}>
      <Navbar.Brand href="/" className={styles.brand}>
        <img 
          src={Logo} 
          className={styles.logo}
          alt={t('title') as string}
          title={t('title') as string} />
          <span className="d-none d-sm-inline">{t('header')}</span>
      </Navbar.Brand>

      <Navbar.Toggle aria-controls="basic-navbar-nav" className={`${styles.toggler} ${expanded ? styles.togglerExpanded : ''}`}>
        <span className={styles.hamburgerLine}/>
      </Navbar.Toggle>

      <Navbar.Collapse id="basic-navbar-nav" className="justify-content-end">
        <Collapse in={expanded}>
          <Nav className={styles.nav}>

            <Dropdown className={styles.dropdown}>
              <Dropdown.Toggle as={CustomToggle} title={t('about.title') as string} />
              <Dropdown.Menu as={CustomMenu} variant="dark">
                <Dropdown.Item href="https://ga.sd.di.huc.knaw.nl/">
                  {t('about.dropdown.item1')}
                </Dropdown.Item>
                <Dropdown.Item href="https://ga-wp3.sd.di.huc.knaw.nl/">
                  {t('about.dropdown.item2')}
                </Dropdown.Item>
                <Dropdown.Item href="https://www.goldenagents.org/">
                  {t('about.dropdown.item3')}      
                </Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>

            <Dropdown className={styles.dropdown}>
              <Dropdown.Toggle as={CustomToggle} title={t('tools.title') as string} />
              <Dropdown.Menu as={CustomMenu} variant="dark">
                <Dropdown.Item href="https://ga.sd.di.huc.knaw.nl/">
                  {t('tools.dropdown.item1')}
                </Dropdown.Item>
                <Dropdown.Item href="https://ga-wp3.sd.di.huc.knaw.nl/">
                  {t('tools.dropdown.item2')}
                </Dropdown.Item>
                <Dropdown.Item href="https://lenticularlens.org/">
                  {t('tools.dropdown.item3')}
                </Dropdown.Item>
                <Dropdown.Item href="#">
                  {t('tools.dropdown.item4')}
                </Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>

            <Dropdown className={styles.dropdown}>
              <Dropdown.Toggle as={CustomToggle} title={t('help.title') as string} />
              <Dropdown.Menu as={CustomMenu} variant="dark">
                <Dropdown.Item href="#">
                  {t('help.dropdown.item1')}
                </Dropdown.Item>
                <Dropdown.Item href="#">
                  {t('help.dropdown.item2')}
                </Dropdown.Item>
                <Dropdown.Item href="#">
                  {t('help.dropdown.item3')}
                </Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>

          </Nav>
        </Collapse>
      </Navbar.Collapse>
    </Navbar>
  )
}

// The forwardRef is important
// Dropdown needs access to the DOM node in order to position the Menu
const CustomToggle = forwardRef(({children, onClick, title, 'aria-expanded': show}: any, ref: Ref<HTMLAnchorElement>) => 
  <a 
    href="#" 
    ref={ref} 
    onClick={e => {
      e.preventDefault();
      onClick && onClick(e);
    }}
    className={`${styles.dropdownToggle} ${show ? styles.dropdownToggleActive : ''}`}>
    {title}
    <img src={AngleDown} className={styles.icon} alt="expand" />
  </a>
);

// forwardRef again here
// Dropdown needs access to the DOM of the Menu to measure it
const CustomMenu = forwardRef(({children, style, className, 'aria-labelledby': labeledBy, show}: DropdownProps, ref: Ref<HTMLDivElement>) =>
  <Collapse in={show} appear={true}> 
    <div
      ref={ref}
      style={style}
      className={`${className} ${styles.dropdownMenu}`}
      aria-labelledby={labeledBy}>
      {children}
    </div>
  </Collapse>
);