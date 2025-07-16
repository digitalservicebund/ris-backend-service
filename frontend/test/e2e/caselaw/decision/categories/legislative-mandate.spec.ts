import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "legislative mandate (Gesetzgebungsauftrag)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4575",
    },
    tag: ["@RISDEV-4575"],
  },
  () => {
    test("legislative mandate (display logic and XML-Vorschau)", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

      await test.step("check that with courType 'AG Aachen' checkbox is hidden and button is hidden", async () => {
        await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
          "AG Aachen",
        )
        await expect(
          page.getByRole("button", { name: "Gesetzgebungsauftrag" }),
        ).toBeHidden()
        await expect(
          page.getByText("Gesetzgebungsauftrag vorhanden"),
        ).toBeHidden()
      })

      await addCourtType(page, "BVerfG")

      await test.step("check button is displayed instead of checkbox when checkbox is unchecked", async () => {
        await expect(
          page.getByRole("button", { name: "Gesetzgebungsauftrag" }),
        ).toBeVisible()
        await expect(
          page.getByText("Gesetzgebungsauftrag vorhanden"),
        ).toBeHidden()
      })

      await test.step("click 'Gesetzgebungsauftrag'-button", async () => {
        await page.getByRole("button", { name: "Gesetzgebungsauftrag" }).click()
      })

      await checkLegislativeMandate(page)

      await reloadWithCheck(page)

      await test.step("remove court type", async () => {
        await page.getByLabel("Gericht", { exact: true }).fill("")
        await save(page)
      })

      await reloadWithCheck(page)

      await addCourtType(page, "BVerfG")

      await test.step("check legislative mandate is visible in preview", async () => {
        await navigateToPreview(
          page,
          prefilledDocumentUnit.documentNumber as string,
        )
        await expect(page.getByText("Gesetzgebungsauftrag")).toBeVisible()
      })

      await test.step("XML preview should display 'Gesetzgebungsauftrag' in 'paratrubriken'", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await page.getByText("XML Vorschau").click()
        const xmlPreview = page.getByTitle("XML Vorschau")
        const innerText = await xmlPreview.innerText()

        const regex =
          /<zuordnung>\s*\d*\s*<aspekt>Gesetzgebungsauftrag<\/aspekt>\s*\d*\s*<begriff>ja<\/begriff>\s*\d*\s*<\/zuordnung>/
        expect(innerText).toMatch(regex)
      })

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

      await test.step("uncheck legislative mandate", async () => {
        const checkBox = page.getByLabel("Gesetzgebungsauftrag")
        await checkBox.uncheck()
        await expect(checkBox).not.toBeChecked()
        await save(page)
      })

      await test.step("check legislative mandate is not visible in preview", async () => {
        await navigateToPreview(
          page,
          prefilledDocumentUnit.documentNumber as string,
        )
        await expect(page.getByText("Gesetzgebungsauftrag")).toBeHidden()
      })

      await test.step("XML preview should not display 'Gesetzgebungsauftrag' in 'paratrubriken'", async () => {
        await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
        await page.getByText("XML Vorschau").click()
        const xmlPreview = page.getByTitle("XML Vorschau")
        const innerText = await xmlPreview.innerText()
        expect(innerText).not.toContain("<aspekt>Gesetzgebungsauftrag</aspekt>")
      })
    })

    async function reloadWithCheck(page: Page) {
      await test.step("reload page and check that checkbox is checked and button is hidden", async () => {
        await page.reload()
        const checkBox = page.getByLabel("Gesetzgebungsauftrag")
        await expect(checkBox).toBeChecked()
        await expect(
          page.getByText("Gesetzgebungsauftrag vorhanden"),
        ).toBeVisible()
        await expect(
          page.getByRole("button", { name: "Gesetzgebungsauftrag" }),
        ).toBeHidden()
      })
    }

    async function addCourtType(page: Page, courtType: string) {
      await test.step(`add court type : ${courtType}`, async () => {
        await page.getByLabel("Gericht", { exact: true }).fill(courtType)
        await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
          courtType,
        )
        await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
          courtType,
        )
        await page.getByText(courtType).click()
        await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
          courtType,
        )

        await save(page)
      })
    }

    async function checkLegislativeMandate(page: Page) {
      await test.step("check legislative mandate", async () => {
        const checkBox = page.getByLabel("Gesetzgebungsauftrag")
        await checkBox.check()
        await expect(checkBox).toBeChecked()
        await save(page)
      })
    }
  },
)
