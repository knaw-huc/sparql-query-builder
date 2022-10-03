import type { ReactNode } from 'react';
import { AnimatePresence, LayoutGroup } from 'framer-motion';
import styles from './Notifications.module.scss';

type Props = {
  children: ReactNode
}

export const NotificationList = ({ children }: Props) => {
  return (
    <ul className={styles.list}>
      <LayoutGroup id="notications">
        <AnimatePresence initial={false}>
          {children}
        </AnimatePresence>
      </LayoutGroup>
    </ul>
  )
}
