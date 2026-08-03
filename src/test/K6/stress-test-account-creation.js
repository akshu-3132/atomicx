import http from "k6/http";
import { check } from "k6";
import exec from "k6/execution";

const BASE_URL = "http://localhost:8080";

export const options = {
  scenarios: {
    seed_accounts: {
      executor: "shared-iterations",
      vus: 50,
      iterations: 10000,
      maxDuration: "10m",
    },
  },
};

export default function () {
  const id = exec.scenario.iterationInTest + 1; // globally unique, 1..10000

  const username = `user${String(id).padStart(4, "0")}`;

  const payload = JSON.stringify({
    firstName: `User${id}`,
    email: `${username}@test.com`,
    userName: username,
  });

  const res = http.post(`${BASE_URL}/api/accounts/create`, payload, {
    headers: { "Content-Type": "application/json" },
  });

  check(res, {
    "created successfully": (r) => r.status === 200 || r.status === 201,
  });

  if (res.status !== 200 && res.status !== 201) {
    console.log(`Failed to create ${username}: ${res.status} - ${res.body}`);
  }
}