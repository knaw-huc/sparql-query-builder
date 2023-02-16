import {AnimatePresence, LayoutGroup} from 'framer-motion';
import styles from './Notifications.module.scss';
import type {NotificationListProps} from '../../types/notifications';

export const NotificationList = ({ children }: NotificationListProps) => 
  <ul className={styles.list}>
    <LayoutGroup id="notications">
      <AnimatePresence initial={false}>
        {children}
      </AnimatePresence>
    </LayoutGroup>
  </ul>
