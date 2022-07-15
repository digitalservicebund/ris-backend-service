const scanner = require("sonarqube-scanner")

// requires "vitest run --coverage" to be run first
// this scanner then picks up the report automatically from ./coverage/lcov.info and uploads it
scanner(
  {
    serverUrl: process.env.SONAR_WEB_URL || "http://localhost:9000",
    token: process.env.SONAR_WEB_TOKEN || "",
    options: {
      "sonar.projectKey": process.env.SONAR_WEB_PROJECTKEY || "neuris-web",
    },
  },
  () => process.exit()
)
