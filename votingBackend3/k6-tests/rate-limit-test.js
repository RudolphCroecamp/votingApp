
import http from 'k6/http';
import { Counter } from 'k6/metrics';
import { sleep } from 'k6';

const limited = new Counter('rate_limited');
const successful = new Counter('successful');
const unexpected = new Counter('unexpected');

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:5000';

export const options = {
    vus: 5,
    duration: '10s',
};

export default function () {
    const response = http.get(`${BASE_URL}/api/Health`);

    if (response.status === 429) {
        limited.add(1);
    } else if (response.status === 200) {
        successful.add(1);
    }else{
        unexpected.add(1);
    }

    sleep(0.1);
}