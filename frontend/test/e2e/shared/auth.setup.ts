import { expect, test as setup } from "@playwright/test"

const authFile = "test/e2e/shared/.auth/user.json"

setup("authenticate user", async ({ page }) => {
  await page.goto("/")
  await page
    .getByLabel("E-Mailadresse")
    .fill(process.env.E2E_TEST_USER as string)
  await page
    .getByLabel("Passwort")
    .fill(process.env.E2E_TEST_PASSWORD as string)
  await page.locator("input#kc-login").click()

  await page.goto(process.env.E2E_BASE_URL ?? "http://127.0.0.1")
  await expect(page.getByText("Übersicht Rechtsprechung")).toBeVisible()

  await page.context().storageState({ path: authFile })
})
