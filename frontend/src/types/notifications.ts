import type {ReactNode} from 'react';

export type NotificationTypes = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id: string;
  message: string;
  type?: NotificationTypes;
  onClose?: () => void;
}

export interface NotificationProps {
  notification: Notification;
}

export interface NotificationListProps {
  children: ReactNode
}

export interface NotificationsState {
  notifications: Notification[]
}