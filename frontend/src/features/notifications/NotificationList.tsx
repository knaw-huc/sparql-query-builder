import type {ReactNode} from 'react';
import {AnimatePresence, LayoutGroup} from 'framer-motion';
import styles from './Notifications.module.scss';

interface NotificationListProps {
  children: ReactNode
}

export const NotificationList = ({ children }: NotificationListProps) => 
  <ul className={styles.list}>
    <LayoutGroup id="notications">
      <AnimatePresence initial={false}>
        {children}
      </AnimatePresence>
    </LayoutGroup>
  </ul>
