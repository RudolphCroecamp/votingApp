
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 100 },
    { duration: '30s', target: 0 },
  ],
};

const BASE_URL = 'http://host.docker.internal:5000';

export default function () {
  const response = http.get(`${BASE_URL}/api/Health`);

  check(response, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}