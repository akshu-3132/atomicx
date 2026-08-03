import http from "k6/http";
import { check } from "k6";
import { Counter, Trend } from "k6/metrics";


http.setResponseCallback(http.expectedStatuses(200, 400));

function generateIdempotencyKey() {
    return `${__VU}-${__ITER}-${Date.now()}-${Math.random().toString(36).substring(2)}`;
}

function randomUser() {
    const id = Math.floor(Math.random() * 1000) + 1;
    return `user${String(id).padStart(4, "0")}`;
}

const successfulTransfers = new Counter("successful_transfers");
const insufficientFunds = new Counter("insufficient_funds_responses");
const notFoundErrors = new Counter("not_found_errors");
const unexpectedErrors = new Counter("unexpected_errors");
const transferDuration = new Trend("transfer_duration", true);

export const options = {
    scenarios: {
        transfers: {
            executor: "constant-arrival-rate",
            rate: 1000,
            timeUnit: "1s",
            duration: "60s",
            preAllocatedVUs: 500,
            maxVUs: 2000,
        },
    },

    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<200"],
        unexpected_errors: ["count<10"],
        not_found_errors: ["count<10"],
    },
};

const BASE_URL = "http://localhost:8080";

export default function () {
    let sender = randomUser();
    let receiver = randomUser();

    while (sender === receiver) {
        receiver = randomUser();
    }

    const payload = JSON.stringify({
        senderUserName: sender,
        receiverUserName: receiver,
        amount: Math.floor(Math.random() * 10) + 1,
    });

    const params = {
        headers: {
            "Content-Type": "application/json",
            "Idempotency-Key": generateIdempotencyKey(),
        },
    };

    const res = http.post(
        `${BASE_URL}/api/transactions/transfer`,
        payload,
        params
    );

    transferDuration.add(res.timings.duration);

    check(res, {
        "status is 200 or 400": (r) => r.status === 200 || r.status === 400,
    });

    if (res.status === 200) {
        successfulTransfers.add(1);
    } else if (res.status === 400) {
        insufficientFunds.add(1);
    } else if (res.status === 404) {
        notFoundErrors.add(1);
    } else {
        unexpectedErrors.add(1);
    }
}