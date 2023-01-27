import { devices, PlaywrightTestConfig, expect, Page } from "@playwright/test"
import { isInViewport } from "./test/e2e/shared/e2e-utils"

const config: PlaywrightTestConfig = {
  testDir: "./test/e2e",
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  globalSetup: "./test/e2e/shared/globalSetup.ts",
  use: {
    viewport: { width: 1280, height: 720 },
    acceptDownloads: true,
    baseURL: process.env.E2E_BASE_URL || "http://127.0.0.1",
    screenshot: "only-on-failure",
    httpCredentials: {
      username: process.env.STAGING_USER ?? "",
      password: process.env.STAGING_PASSWORD ?? "",
    },
    storageState: "test/e2e/shared/storageState.json",
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

expect.extend({
  async toHaveInsideViewport(page: Page, selector: string) {
    return {
      pass: await isInViewport(page, selector, true),
      message: () => `${selector} is outside viewport!`,
    }
  },
  async toHaveOutsideViewport(page: Page, selector: string) {
    return {
      pass: await isInViewport(page, selector, false),
      message: () => `${selector} is inside viewport!`,
    }
  },
})

export default config
