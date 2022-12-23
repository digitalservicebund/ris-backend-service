import { devices } from "@playwright/test"
import config from "../../playwright.config"

config.testDir = "."
config.projects = [
  {
    name: "chromium",
    use: { ...devices["Desktop Chrome"], channel: "chrome" },
  },
]

export default config
