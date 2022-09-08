## Run

```sh
docker-compose up
```

### Spring Boot

Is exposing metrics at: `localhost:8080/actuator/prometheus`

### Prometheus

Prometheus is scraping those and makes them available for querying at: `localhost:9090`

### Grafana

Grafana is using Prometheus as a datasource and can be used to explore and visualise metrics: `localhost:3030`.

The login is `admin` / `admin`.

Import the dashboards in `grafana/dashboards` here: `http://localhost:3030/dashboard/import`

These two could be useful out of the box. They can be imported using their dashboard id:

- https://grafana.com/grafana/dashboards/4701-jvm-micrometer/
- https://grafana.com/grafana/dashboards/11378-justai-system-monitor/
