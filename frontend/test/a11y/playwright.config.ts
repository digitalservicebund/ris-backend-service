import { devices } from "@playwright/test"
import config from "../../playwright.config"

config.testDir = "."
config.globalSetup = "../e2e/shared/globalSetup.ts"
config.projects = [
  {
    name: "chromium",
    use: { ...devices["Desktop Chrome"], channel: "chrome" },
  },
]

export default config
