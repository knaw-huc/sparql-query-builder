import {createSlice, PayloadAction} from '@reduxjs/toolkit';
import {v4 as uuidv4} from 'uuid';
import {RootState} from '../../app/store';
import {useAppSelector} from '../../app/hooks';
import type {Notification, NotificationsState} from '../../types/notifications';

const initialState: NotificationsState = {
  notifications: [],
}

const notificationsSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    addNotification: (
      state,
      {payload}: PayloadAction<Omit<Notification, 'id'>>
    ) => {
      const notification: Notification = {
        id: uuidv4(),
        ...payload,
      }

      state.notifications.push(notification)
    },
    dismissNotification: (
      state,
      {payload}: PayloadAction<Notification['id']>
    ) => {
      const index = state.notifications.findIndex(
        (notification) => notification.id === payload
      )

      if (index !== -1) {
        state.notifications.splice(index, 1)
      }
    },
  },
})

const {reducer, actions } = notificationsSlice

export const {
  addNotification,
  dismissNotification,
} = actions

// Selectors
const selectNotifications = (state: RootState) =>
  state.notifications.notifications

// Hooks
export const useNotifications = () => useAppSelector(selectNotifications)

export default reducer
