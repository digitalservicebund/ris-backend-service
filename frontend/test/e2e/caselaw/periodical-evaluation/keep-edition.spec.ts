import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillInput,
  navigateToCategories,
  navigateToPeriodicalReferences,
  save,
  searchForDocUnitWithFileNumberAndDecisionDate,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Keep edition and edition rank when editing doc unit",
  {
    tag: "@RISDEV-6082",
  },

  () => {
    test("Editing documentation unit with references that were created during periodical evaluation keeps relationship between reference and edition", async ({
      page,
      edition,
      prefilledDocumentUnit,
    }) => {
      const suffix = edition?.suffix
      const fileNumber = prefilledDocumentUnit.coreData?.fileNumbers?.[0]

      await navigateToPeriodicalReferences(page, edition.id || "")

      await test.step("Add caselaw reference", async () => {
        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber || "",
          "31.12.2019",
        )
        // wait search result to be visible
        const searchResultsContainer = page.getByTestId("search-results")
        await expect(
          searchResultsContainer.getByTestId(
            `decision-summary-${prefilledDocumentUnit.documentNumber}`,
          ),
        ).toBeVisible()
        // wait for panel to open
        await expect(page).toHaveURL(/showAttachmentPanel=true/)

        await fillInput(page, "Zitatstelle *", "123")
        await fillInput(page, "Klammernzusatz", "L")

        const putRequestPromise = page.waitForRequest((request) =>
          request.url().includes("api/v1/caselaw/legalperiodicaledition"),
        )
        await page.getByLabel("Treffer übernehmen").click()

        await putRequestPromise

        await expect(
          page.getByText(`MMG 2024, 123${suffix} (L)`, { exact: true }),
        ).toHaveCount(1)
        await expect(page).toHaveURL(/showAttachmentPanel=false/)
      })

      await test.step("Add literature reference", async () => {
        await searchForDocUnitWithFileNumberAndDecisionDate(
          page,
          fileNumber || "",
          "31.12.2019",
        )
        // wait search result to be visible
        const searchResultsContainer = page.getByTestId("search-results")
        await expect(
          searchResultsContainer.getByTestId(
            `decision-summary-${prefilledDocumentUnit.documentNumber}`,
          ),
        ).toBeVisible()

        // wait for panel to open
        await expect(page).toHaveURL(/showAttachmentPanel=true/)

        await page.getByLabel("Literatur Fundstelle").click()
        await fillInput(page, "Zitatstelle *", "124")
        await fillInput(page, "Autor Literaturfundstelle", "Berg, Peter")
        await fillInput(page, "Dokumenttyp Literaturfundstelle", "Ean")
        await page.getByText("Ean", { exact: true }).click()
        await expect(
          page.getByLabel("Dokumenttyp Literaturfundstelle", { exact: true }),
        ).toHaveValue("Anmerkung")

        const putRequestPromise = page.waitForRequest((request) =>
          request.url().includes("api/v1/caselaw/legalperiodicaledition"),
        )
        await page.getByLabel("Treffer übernehmen").click()

        await putRequestPromise

        await expect(
          page.getByText(`MMG 2024, 124${suffix}, Berg, Peter (Ean)`, {
            exact: true,
          }),
        ).toHaveCount(1)
      })

      await test.step("Navigate to documentation unit categories, add file number and save", async () => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

        await page.getByLabel("Aktenzeichen").getByRole("textbox").fill("test")
        await page.keyboard.press("Enter")

        await save(page)
      })

      await test.step("Navigate to edition and check that references are still there", async () => {
        await navigateToPeriodicalReferences(page, edition.id || "")
        await expect(
          page.getByText(`MMG 2024, 123${suffix} (L)`, { exact: true }),
        ).toHaveCount(1)
        await expect(
          page.getByText(`MMG 2024, 124${suffix}, Berg, Peter (Ean)`, {
            exact: true,
          }),
        ).toHaveCount(1)
      })
    })
  },
)
