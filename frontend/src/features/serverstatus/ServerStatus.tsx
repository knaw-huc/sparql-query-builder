import React from 'react';
import { useServerStatusQuery } from './serverStatusApi';
import styles from './ServerStatus.module.scss';

// Polling interval for getting backend server status
const interval = 10000;

export const ServerStatus = () => {
  const {
    data,
    error,
  } = useServerStatusQuery(undefined, {
    pollingInterval: interval,
  });

  return (
    <p>
      { data?.online && !error ? 'Backend online' : 'Backend offline' }
    </p>
  );
}
