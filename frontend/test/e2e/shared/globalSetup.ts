import { chromium, firefox, expect, FullConfig } from "@playwright/test"

export default async function globalSetup(config: FullConfig) {
  const baseURL = config.projects[0].use.baseURL as string
  let browser

  try {
    browser = await chromium.launch()
  } catch (e) {
    browser = await firefox.launch()
  }

  const page = await browser.newPage()
  await page.goto(baseURL)
  await page
    .getByLabel("E-Mailadresse")
    .fill(process.env.E2E_TEST_USER as string)
  await page
    .getByLabel("Passwort")
    .fill(process.env.E2E_TEST_PASSWORD as string)
  await page.locator("input#kc-login").click()

  await page.goto(process.env.E2E_BASE_URL ?? "http://127.0.0.1")
  await expect(page.getByText("Übersicht Rechtsprechung")).toBeVisible()

  await page
    .context()
    .storageState({ path: "test/e2e/shared/storageState.json" })
}
