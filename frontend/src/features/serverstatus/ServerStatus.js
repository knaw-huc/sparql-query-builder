import React from 'react';
import { useServerStatusQuery } from './serverStatusApi';
import styles from './ServerStatus.module.scss';

// Polling interval for getting server status
const interval = 10000;

export function ServerStatus() {
  const {
    data,
    error,
    isLoading,
    isFetching,
    refetch,
  } = useServerStatusQuery(undefined, {
    pollingInterval: interval,
  });

  return (
    <div>
      <p>Server status test</p>
      { data?.online ? <p>Server online</p> : <p>Server offline</p> }
    </div>
  );
}
