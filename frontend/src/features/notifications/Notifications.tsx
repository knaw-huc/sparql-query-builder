import {useNotifications} from './notificationsSlice';
import {NotificationItem} from './NotificationItem';
import {NotificationList} from './NotificationList';
import type {Notification} from './NotificationItem';

export function Notifications() {
  const notifications = useNotifications();

  return (
    <NotificationList>
      {notifications.map((notification: Notification) => (
        <NotificationItem key={notification.id} notification={notification} />
      ))}
    </NotificationList>
  )
}
