import { defineConfig, devices } from "@playwright/test"

const config = defineConfig({
  testDir: "./test/e2e",
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 4 : undefined,
  fullyParallel: true,
  timeout: 120000,
  expect: { timeout: 5000 },
  reporter: process.env.CI
    ? [
        ["dot"],
        ["json", { outputFile: "test-results.json" }],
        ["blob", { outputFile: "./blob-report/test-report.zip" }],
      ]
    : "list",
  use: {
    viewport: { width: 1280, height: 720 },
    acceptDownloads: true,
    baseURL: process.env.E2E_BASE_URL ?? "http://127.0.0.1",
    screenshot: { mode: "only-on-failure", fullPage: true },
    timezoneId: "Europe/Berlin",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "setup-chromium",
      use: { ...devices["Desktop Chrome"] },
      testMatch: /.*.setup.ts$/,
    },
    {
      name: "setup-firefox",
      use: { ...devices["Desktop Firefox"] },
      testMatch: /.*.setup.ts$/,
    },
    {
      name: "chromium",
      testDir: "./test/e2e",
      use: {
        ...devices["Desktop Chrome"],
        channel: "chrome",
        storageState: "test/e2e/caselaw/.auth/user.json",
      },
      dependencies: ["setup-chromium"],
    },
    {
      name: "firefox",
      testDir: "./test/e2e",
      use: {
        ...devices["Desktop Firefox"],
        storageState: "test/e2e/caselaw/.auth/user.json",
      },
      dependencies: ["setup-firefox"],
    },
    {
      name: "a11y",
      testDir: "./test/a11y",
      use: {
        ...devices["Desktop Chrome"],
        channel: "chrome",
        storageState: "test/e2e/caselaw/.auth/user.json",
      },
      dependencies: ["setup-chromium"],
    },
    {
      name: "queries",
      testDir: "./test/queries",
      use: {
        ...devices["Desktop Chrome"],
        channel: "chrome",
        storageState: "test/e2e/caselaw/.auth/user.json",
      },
      dependencies: ["setup-chromium"],
    },
  ],
})

export default config
