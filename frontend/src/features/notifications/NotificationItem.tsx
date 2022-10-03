import { ReactNode } from 'react';
import { motion, useIsPresent, type Variants } from 'framer-motion';
import { useTimeoutFn, useUpdateEffect } from 'react-use';
import { dismissNotification } from './notificationsSlice';
import { useAppSelector, useAppDispatch } from '../../app/hooks';
import styles from './Notifications.module.scss';

export type NotificationTypes = 'success' | 'error' | 'warning' | 'info';

export type Notification = {
  /**
   * The notification id.
   */
  id: string

  /**
   * The message of the notification
   */
  message: string

  /**
   * dismiss duration time
   */
  autoHideDuration?: number

  /**
   * The type of notification to show.
   */
  type?: NotificationTypes

  /**
   * Optional callback function to run side effects after the notification has closed.
   */
  onClose?: () => void

  /**
   * Optionally add an action to the notification through a ReactNode
   */
  action?: ReactNode
}

type Props = {
  notification: Notification
}

export const NotificationItem = ({
  notification: { id, autoHideDuration, message, onClose, type = 'info' },
}: Props) => {
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
      className={ [styles.listItem, styles[type]].join(' ') }
      initial={{ opacity: 0, y: "100%", scale: 1 }}
      animate={{ opacity: 1, y: 0, x: 0, scale: 1 }}
      exit={{ opacity: 0, x: "100%", scale: 1 }}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}>
      <div className={styles.text}>{message}</div>
      <button onClick={handleDismiss} className={styles.close}>
      </button>
    </motion.li>
  )
}
