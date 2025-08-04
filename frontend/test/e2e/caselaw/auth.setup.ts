import fs from "fs"
import { expect, test as setup } from "@playwright/test"

function authenticateUser(user: {
  name: string
  email: string
  password: string
}) {
  setup(`authenticate ${user.name}`, async ({ page, browser }) => {
    const cookieFile = `test/e2e/caselaw/.auth/${user.name}.json`
    if (fs.existsSync(cookieFile)) {
      const context = await browser.newContext({
        storageState: cookieFile,
      })

      const cookies = await context.cookies()
      let sessionCookie = null
      cookies.forEach((cookie) => {
        if (cookie.name === "SESSION") {
          sessionCookie = cookie
        }
      })

      if (sessionCookie !== null) {
        return
      }
    }

    await page.goto("/")
    await page.fill("#username", user.email)
    await page.fill("#password", user.password)
    await page.locator("input#kc-login").click()

    await page.goto(process.env.E2E_BASE_URL ?? "http://127.0.0.1")
    // Caution: The test needs to wait for the page to load completely
    // Otherwise, the location cookie might not be unset and redirect to '/' on page load
    await expect(page.getByText("Starten Sie die Suche")).toBeVisible()

    await page
      .context()
      .storageState({ path: `test/e2e/caselaw/.auth/${user.name}.json` })
  })
}

;[
  {
    name: "user",
    email: process.env.E2E_TEST_USER as string,
    password: process.env.E2E_TEST_PASSWORD as string,
  },
  {
    name: "user_bgh",
    email: process.env.E2E_TEST_USER_BGH as string,
    password: process.env.E2E_TEST_PASSWORD_BGH as string,
  },
  {
    name: "user_bfh",
    email: process.env.E2E_TEST_USER_BFH as string,
    password: process.env.E2E_TEST_PASSWORD_BFH as string,
  },
  {
    name: "user_external",
    email: process.env.E2E_TEST_USER_EXTERNAL as string,
    password: process.env.E2E_TEST_PASSWORD_EXTERNAL as string,
  },
].forEach(authenticateUser)
