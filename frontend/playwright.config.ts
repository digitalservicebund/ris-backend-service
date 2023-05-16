import { devices, PlaywrightTestConfig } from "@playwright/test"

const config: PlaywrightTestConfig = {
  testDir: "./test/e2e/norms",
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 4 : undefined,
  fullyParallel: true,
  globalSetup: "test/e2e/shared/globalSetup.ts",
  use: {
    viewport: { width: 1280, height: 720 },
    acceptDownloads: true,
    baseURL: process.env.E2E_BASE_URL ?? "http://127.0.0.1",
    screenshot: { mode: "only-on-failure", fullPage: true },
    storageState: "test/e2e/shared/storageState.json",
    timezoneId: "Europe/Berlin",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"], channel: "chrome" },
    },
    {
      name: "firefox",
      use: { ...devices["Desktop Firefox"] },
    },
  ],
}

export default config
