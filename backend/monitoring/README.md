## Run

```sh
docker compose up
```

### Spring Boot

Is exposing metrics at: http://127.0.0.1/actuator/prometheus

### Prometheus

Prometheus is scraping those and makes them available for querying at: `localhost:9090`

### Grafana

#### Our dashboards

Grafana is using Prometheus as a datasource and can be used to explore and visualise metrics: `localhost:3030`.

The login is `admin` / `admin`.

Add `http://prometheus:9090` as a datasource.

Import the dashboards in `grafana/dashboards` here: `http://localhost:3030/dashboard/import`. Note that the `uid` of the datasource has to be adjusted to changed via search & replace in the `.json` file before importing --> from `2GV29hM4k` to your actual one.

#### Readymade dashboards

These two are useful out of the box. They can be imported using their dashboard id:

- https://grafana.com/grafana/dashboards/4701-jvm-micrometer/
- https://grafana.com/grafana/dashboards/11378-justai-system-monitor/
