import {motion, useIsPresent} from 'framer-motion';
import {useTimeoutFn, useUpdateEffect} from 'react-use';
import {dismissNotification} from './notificationsSlice';
import {useAppDispatch} from '../../app/hooks';
import styles from './Notifications.module.scss';

export type NotificationTypes = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id: string
  message: string
  type?: NotificationTypes
  onClose?: () => void
}

interface NotificationProps {
  notification: Notification
}

export function NotificationItem({
  notification: {id, message, onClose, type = 'info'},
}: NotificationProps) {
  const dispatch = useAppDispatch()
  const isPresent = useIsPresent()

  // Handle dismiss of a single notification
  const handleDismiss = () => {
    if (isPresent) {
      dispatch(dismissNotification(id))
    }
  }

  // Call the dismiss function after a certain timeout
  const [, cancel, reset] = useTimeoutFn(
    handleDismiss,
    6000
  )

  // Reset or cancel dismiss timeout based on mouse interactions
  const onMouseEnter = () => cancel()
  const onMouseLeave = () => reset()

  // Call `onDismissComplete` when notification unmounts if present
  useUpdateEffect(() => {
    if (!isPresent) {
      onClose?.()
    }
  }, [isPresent])

  return (
    <motion.li
      layout
      initial={{opacity: 0, y: 50, scale: 0.3}}
      animate={{opacity: 1, y: 0, scale: 1}}
      exit={{opacity: 0, scale: 0.5, transition: {duration: 0.2}}}
      className={[styles.listItem, styles[type]].join(' ') }
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}>
      <div className={styles.text}>
        <span dangerouslySetInnerHTML={{__html: message}} />
      </div>
      <button onClick={handleDismiss} className={styles.close} />
    </motion.li>
  )
}
