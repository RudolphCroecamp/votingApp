Run the following command while in the backend root directory:

docker run --rm `
  --network bridge `
  -e BASE_URL=http://host.docker.internal:5000 `
  -v "${PWD}/k6-tests:/scripts:ro" `
  grafana/k6 run /scripts/health-test.js


docker run --rm `
  --network bridge `
  -e BASE_URL=http://host.docker.internal:5000 `
  -v "${PWD}/k6-tests:/scripts:ro" `
  grafana/k6 run /scripts/rate-limit-test.js