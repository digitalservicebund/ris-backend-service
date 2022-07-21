import { devices, PlaywrightTestConfig } from "@playwright/test"

const config: PlaywrightTestConfig = {
  testDir: "./test/e2e",
  timeout: 40000,
  retries: process.env.CI === "true" ? 1 : 0,
  workers: process.env.CI ? 8 : undefined,
  use: {
    viewport: { width: 1280, height: 720 },
    acceptDownloads: true,
    baseURL: process.env.E2E_BASE_URL || "http://localhost:3000",
    screenshot: "only-on-failure",
    httpCredentials: {
      username: process.env.STAGING_USER ?? "",
      password: process.env.STAGING_PASSWORD ?? "",
    },
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
    {
      name: "webkit",
      use: { ...devices["Desktop Safari"] },
    },
    {
      name: "edge",
      use: {
        channel: "msedge",
      },
    },
  ],
}

export default config
