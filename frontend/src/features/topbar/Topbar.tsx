import {useState, useRef, useEffect} from 'react';
import Navbar from 'react-bootstrap/Navbar';
import styles from './Topbar.module.scss';
import Logo from "../../images/logo-ga.png";
import {useTranslation} from 'react-i18next';
import AngleDown from '../../images/angle-down-solid.svg';
import {AnimatePresence, LayoutGroup} from 'framer-motion';
import {LayoutMotionDiv, SlideDownDiv} from '../animations/Animations';
import {useBreakpoints} from 'react-hook-breakpoints';
import type {MenuWrapProps, DropperProps, ToggleProps, MenuProps, ItemProps} from '../../types/topbar';

const menuStructure = [
  {
    title: 'about',
    children: [
      {title: 'item1', link: 'https://ga.sd.di.huc.knaw.nl/'},
      {title: 'item2', link: 'https://ga-wp3.sd.di.huc.knaw.nl/'},
      {title: 'item3', link: 'https://www.goldenagents.org/'},
    ],
  },
  {
    title: 'tools',
    children: [
      {title: 'item1', link: 'https://ga.sd.di.huc.knaw.nl/'},
      {title: 'item2', link: 'https://ga-wp3.sd.di.huc.knaw.nl/'},
      {title: 'item3', link: 'https://lenticularlens.org/'},
      {title: 'item4', link: '#'},
    ],
  },
  {
    title: 'help',
    children: [
      {title: 'item1', link: '#'},
      {title: 'item2', link: '#'},
      {title: 'item3', link: '#'},
    ],
  },
];

export const Topbar = () => {
  const {t} = useTranslation(['topbar']);
  const [expanded, setExpanded] = useState(false);
  const [openMenu, setOpenMenu] = useState('');
  const {breakpoints, currentBreakpoint} = useBreakpoints();
  const isSmall = breakpoints[currentBreakpoint] < breakpoints.large;

  return (
    <Navbar 
      bg="primary" 
      variant="light" 
      expand="lg" 
      className={styles.topbar}>
      <Navbar.Brand className={styles.brand}>
        <img 
          src={Logo} 
          className={styles.logo}
          alt={t('title') as string}
          title={t('title') as string} />
          <span className="d-none d-sm-inline">{t('header')}</span>
      </Navbar.Brand>

      {isSmall &&
        <div className={`${styles.toggler} ${expanded ? styles.togglerExpanded : ''}`} onClick={() => setExpanded(!expanded)}>
          <span className={styles.hamburgerLine}/>
        </div>
      }

      <LayoutGroup>
        <MenuWrap expanded={expanded} isSmall={isSmall}>
          {menuStructure.map((item, i) =>
            <Dropper key={`dropdown${i}`} number={i}>
              <Toggle 
                title={t(`${item.title}.title`) as string} 
                open={openMenu === item.title}
                id={item.title}
                onClick={() => setOpenMenu(openMenu === item.title ? '' : item.title)} />
                <Menu 
                  key={item.title} 
                  closeMenu={() => setOpenMenu('')} 
                  open={openMenu === item.title} 
                  isSmall={isSmall}>
                  {item.children.map((child, j) =>
                    <Item 
                      key={`item${j}`}
                      href={child.link} 
                      title={t(`${item.title}.dropdown.${child.title}`)} />
                  )}
                </Menu>
            </Dropper>
          )}
        </MenuWrap>
      </LayoutGroup>

    </Navbar>
  )
}

const MenuWrap = ({children, isSmall, expanded}: MenuWrapProps) =>
  !isSmall ?
    <div className={styles.nav}>
      {children}
    </div>
  :
    <AnimatePresence>
      {expanded &&
        <SlideDownDiv className={`${styles.nav} ${expanded ? styles.navExpanded : ''}`} layout="position">
          {children}
        </SlideDownDiv>
      }
    </AnimatePresence>

const Dropper = ({children, number}: DropperProps) =>
  <LayoutMotionDiv className={`${styles.dropdown} ${number === 0 ? styles.dropdownFirst : ''}`} layout="size">
    {children}
  </LayoutMotionDiv>

const Toggle = ({title, open, onClick, id}: ToggleProps) =>
  <button className={`${styles.dropdownToggle} ${open ? styles.dropdownToggleActive : ''}`} onClick={onClick} id={`menu-toggler-${id}`}>
    <LayoutMotionDiv>
      {title}
      <img src={AngleDown} className={styles.icon} alt="expand" />
    </LayoutMotionDiv>
  </button>

const Menu = ({children, closeMenu, open, isSmall}: MenuProps) => {
  const menuRef = useRef<HTMLDivElement>(null);
  const innerRef = useRef<HTMLDivElement>(null);
  const [height, setHeight] = useState(0);

  useEffect(() => {
    setHeight((innerRef && innerRef.current?.clientHeight) || 0);
  }, []);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (
        menuRef.current && !menuRef.current.contains(target) &&
        target.id.indexOf('menu-toggler') === -1
      ) {
        document.removeEventListener('click', handleClickOutside, true);
        closeMenu();
      }
    };
    if(open) {
      document.addEventListener('click', handleClickOutside, true);
    } 
  });

  return (
    <div 
      className={`${styles.dropdownMenu} ${open ? styles.dropdownMenuOpen : ''}`} 
      style={{maxHeight: open ? height : 0}}
      ref={!isSmall ? menuRef : undefined}>
      <div ref={innerRef}>
        {children}
      </div>
    </div>
  )
}

const Item = ({href, title}: ItemProps) =>
  <a href={href} className={styles.dropdownMenuLink}>
    {title}
  </a>