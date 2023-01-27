import { chromium, expect } from "@playwright/test"

export default async function globalSetup() {
  const browser = await chromium.launch()
  const page = await browser.newPage()
  await page.goto(process.env.E2E_BASE_URL ?? "http://127.0.0.1")
  await page
    .getByLabel("E-Mailadresse")
    .fill(process.env.E2E_TEST_USER as string)
  await page
    .getByLabel("Passwort")
    .fill(process.env.E2E_TEST_PASSWORD as string)
  await page.locator("input#kc-login").click()
  await expect(page.getByText("Anmelden bei ")).toBeHidden()

  await page
    .context()
    .storageState({ path: "test/e2e/shared/storageState.json" })
}
