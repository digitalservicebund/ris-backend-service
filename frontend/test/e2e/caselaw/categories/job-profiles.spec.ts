import { expect } from "@playwright/test"
import { navigateToCategories, save } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

test.describe(
  "job profiles (Berufsbild)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4571",
    },
  },
  () => {
    const firstJobProfile = "Handwerker"
    const secondJobProfile = "Elektriker"

    test("rendering logic", async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("check button is displayed when field is empty", async () => {
        await expect(
          page.getByRole("button", { name: "Berufsbild" }),
        ).toBeVisible()
      })

      await test.step("enter job profile", async () => {
        await page.getByRole("button", { name: "Berufsbild" }).click()

        await page.locator("[aria-label='Berufsbild']").fill(firstJobProfile)
        await page.keyboard.press("Enter")
        await expect(page.getByText(firstJobProfile)).toBeVisible()
      })

      await save(page)

      await test.step("reload page and check that input field and content are visible", async () => {
        await page.reload()
        await expect(
          page.getByRole("heading", { name: "Berufsbild" }),
        ).toBeVisible()
        await expect(page.getByText(firstJobProfile)).toBeVisible()
      })

      await test.step("remove job profile", async () => {
        await page
          .locator("[data-testid='chip']", { hasText: firstJobProfile })
          .getByLabel("Löschen")
          .click()

        await expect(page.getByText(firstJobProfile)).toBeHidden()
      })

      await save(page)

      await test.step("reload page and check that button is visible", async () => {
        await page.reload()

        await expect(
          page.getByRole("button", { name: "Berufsbild" }),
        ).toBeVisible()
      })
    })

    test("add job profiles", async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await expect(
        page.getByRole("button", { name: "Berufsbild" }),
      ).toBeVisible()

      await page.getByRole("button", { name: "Berufsbild" }).click()

      await page.locator("[aria-label='Berufsbild']").fill(firstJobProfile)
      await page.keyboard.press("Enter")
      await expect(page.getByText(firstJobProfile)).toBeVisible()

      await page.locator("[aria-label='Berufsbild']").fill(secondJobProfile)
      await page.keyboard.press("Enter")
      await expect(page.getByText(secondJobProfile)).toBeVisible()
    })

    test("delete job profiles", async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await expect(
        page.getByRole("button", { name: "Berufsbild" }),
      ).toBeVisible()

      await page.getByRole("button", { name: "Berufsbild" }).click()

      await page.locator("[aria-label='Berufsbild']").fill(firstJobProfile)
      await page.keyboard.press("Enter")
      await expect(page.getByText(firstJobProfile)).toBeVisible()

      await page.locator("[aria-label='Berufsbild']").fill(secondJobProfile)
      await page.keyboard.press("Enter")
      await expect(page.getByText(secondJobProfile)).toBeVisible()

      await page
        .locator("[data-testid='chip']", { hasText: firstJobProfile })
        .getByLabel("Löschen")
        .click()

      await expect(page.getByText(firstJobProfile)).toBeHidden()
      await expect(page.getByText(secondJobProfile)).toBeVisible()
    })
  },
)
