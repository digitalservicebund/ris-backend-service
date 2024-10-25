import { expect } from "@playwright/test"
import {
  assignProcedureToDocUnit,
  deleteAllProcedures,
  navigateToCategories,
  navigateToProcedures,
  save,
} from "../e2e-utils"

import { caselawTest as test } from "../fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("Procedures search list (Vorgänge)", () => {
  // If tests run in parallel, we do not want to delete other procedures -> random prefix
  const procedurePrefix = `test_${generateString({ length: 10 })}`

  test("Search for procedures by name", async ({
    page,
    prefilledDocumentUnit,
    secondPrefilledDocumentUnit,
    documentNumber,
  }) => {
    const testcasePrefix = procedurePrefix + generateString({ length: 5 })
    const procedureName1 = await assignProcedureToDocUnit(
      page,
      documentNumber,
      testcasePrefix,
    )
    const procedureName2 = await assignProcedureToDocUnit(
      page,
      prefilledDocumentUnit.documentNumber!,
      testcasePrefix,
    )
    const procedureName3 = await assignProcedureToDocUnit(
      page,
      secondPrefilledDocumentUnit.documentNumber!,
      testcasePrefix,
    )

    await navigateToProcedures(page)

    await test.step("Initially, without a search query all procedures are visible", async () => {
      await expect(page.getByText(procedureName1)).toBeVisible()
      await expect(page.getByText(procedureName2)).toBeVisible()
      await expect(page.getByText(procedureName3)).toBeVisible()
      // There might be other pre-existing procedures
      expect(
        await page.getByLabel("Vorgang Listenelement").count(),
      ).toBeGreaterThanOrEqual(3)
    })

    await test.step("Search for all 3 newly created procedures", async () => {
      await page.getByPlaceholder("Nach Vorgängen suchen").fill(testcasePrefix)

      await expect(page.getByLabel("Vorgang Listenelement")).toHaveCount(3)
      await expect(page.getByText("3 Ergebnisse gefunden")).toBeVisible()
    })

    await test.step("Search for one specific procedure", async () => {
      await page.getByPlaceholder("Nach Vorgängen suchen").fill(procedureName1)

      await expect(page.getByLabel("Vorgang Listenelement")).toHaveCount(1)
      await expect(page.getByText("1 Ergebnis gefunden")).toBeVisible()
      await expect(page.getByText(procedureName1)).toBeVisible()
    })

    await test.step("Search with non-existing name", async () => {
      await page
        .getByPlaceholder("Nach Vorgängen suchen")
        .fill(procedureName1 + "Random")

      await expect(page.getByLabel("Vorgang Listenelement")).toBeHidden()
    })
  })

  test("Load doc unit of a procedure and delete it", async ({
    page,
    context,
    request,
  }) => {
    const procedureName = procedurePrefix + generateString({ length: 5 })

    await navigateToProcedures(page)

    let newDocumentNumber: string
    await test.step("Create new doc unit and assign to same procedure", async () => {
      const cookies = await context.cookies()
      const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
      const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      })

      const { documentNumber } = await response.json()
      newDocumentNumber = documentNumber

      await navigateToCategories(page, documentNumber)

      await page.locator("[aria-label='Vorgang']").fill(procedureName)
      await page.getByText(`${procedureName} neu erstellen`).click()
      await save(page)
    })

    await test.step("Search for procedure", async () => {
      await navigateToProcedures(page)
      await page.getByPlaceholder("Nach Vorgängen suchen").fill(procedureName)
      await expect(page.getByLabel("Vorgang Listenelement")).toHaveCount(1)
      await expect(page.getByText(procedureName)).toBeVisible()
    })

    await test.step("Load document units of procedure", async () => {
      await page.getByTestId("icons-open-close").click()
      await expect(page.getByText(newDocumentNumber)).toBeVisible()
    })

    await test.step("Delete document unit and confirm dialog", async () => {
      await page.getByLabel("Dokumentationseinheit löschen").click()
      await expect(
        page.getByText("Dokumentationseinheit löschen"),
      ).toBeVisible()
      await page.getByLabel("Löschen", { exact: true }).click()
    })

    await test.step("Show message for procedure without doc units", async () => {
      await expect(
        page.getByText("Keine Dokeinheiten sind zugewiesen."),
      ).toBeVisible()
    })
  })

  test.afterAll(async ({ browser }) => {
    await deleteAllProcedures(browser, procedurePrefix)
  })
})
