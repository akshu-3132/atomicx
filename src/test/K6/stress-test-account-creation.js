import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    constant_request_rate: {
      executor: 'constant-arrival-rate',
      rate: 1000,              // 1000 iterations per second
      timeUnit: '1s',
      duration: '1m',          // Run for 1 minute
      preAllocatedVUs: 100,    // Start with 100 workers
      maxVUs: 1000,            // Scale up to 1000 workers if responses get slow
    },
  },
};

export default function () {
  const uniqueId = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  const payload = JSON.stringify({
    firstName: 'TestUser',
    email: `user_${uniqueId}@test.com`,
    userName: `user_${uniqueId}`,
  });

  const res = http.post('http://localhost:8080/api/accounts/create', payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'success': (r) => r.status === 200});
}