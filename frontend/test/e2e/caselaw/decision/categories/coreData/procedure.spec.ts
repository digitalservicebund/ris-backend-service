import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  deleteDocumentUnit,
  deleteProcedure,
} from "~/e2e/caselaw/utils/documentation-unit-api-util"
import {
  navigateToCategories,
  navigateToProcedures,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("procedure", () => {
  // If tests run in parallel, we do not want to delete other procedures -> random prefix
  const testPrefix = generateString({ length: 10 })
  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage()
    await navigateToProcedures(page, testPrefix)
    const listItems = page.getByLabel("Vorgang Listenelement")
    await expect(listItems).toHaveCount(0)
  })
  test("add new procedure in coreData", async ({
    page,
    documentNumber,
    pageWithBghUser,
  }) => {
    let newProcedure: string

    await test.step("create new procedure", async () => {
      await navigateToCategories(page, documentNumber)

      await expect(async () => {
        newProcedure = testPrefix + generateString({ length: 10 })
        await page.getByLabel("Vorgang", { exact: true }).fill(newProcedure)
        await page.getByText(`${newProcedure} neu erstellen`).click()
      }).toPass()

      await save(page)

      await page.reload()
      await expect(page.getByLabel("Vorgang", { exact: true })).toHaveValue(
        newProcedure,
      )
    })

    await test.step("fill previous procedures", async () => {
      await expect(async () => {
        const secondProcedure = testPrefix + generateString({ length: 10 })
        await page.getByLabel("Vorgang", { exact: true }).fill(secondProcedure)
        await page.getByText(`${secondProcedure} neu erstellen`).click()
      }).toPass()

      await save(page)
      await page.reload()

      // If deviating data is available, it is automatically expanded
      await expect(page.getByText("Vorgangshistorie")).toBeVisible()
      await expect(
        page
          .getByTestId("chips-input-wrapper_previousProcedures")
          .getByText(newProcedure),
      ).toBeVisible()
    })

    await test.step("reuse created procedure", async () => {
      await page.goto("/caselaw")
      await page.getByText("Neue Entscheidung").first().click()
      await page.getByText("Rubriken").click()

      await page
        .getByLabel("Vorgang", { exact: true })
        .fill(newProcedure.substring(0, 7))
      await expect(page.getByText(newProcedure)).toBeVisible()

      await deleteDocumentUnit(
        page,
        /caselaw\/documentunit\/(.*)\/categories/g.exec(
          page.url(),
        )?.[1] as string,
      )
    })

    await test.step("new procedure not visible for different docOffice", async () => {
      await pageWithBghUser.goto("/caselaw")
      await pageWithBghUser.getByText("Neue Entscheidung").first().click()
      await pageWithBghUser.getByText("Rubriken").click()

      await pageWithBghUser
        .getByLabel("Vorgang", { exact: true })
        .fill(newProcedure.substring(0, 7))
      await expect(pageWithBghUser.getByText(newProcedure)).toBeHidden()

      await deleteDocumentUnit(
        pageWithBghUser,
        /caselaw\/documentunit\/(.*)\/categories/g.exec(
          pageWithBghUser.url(),
        )?.[1] as string,
      )
    })
  })
  test.afterAll(async ({ browser }) => {
    const page = await browser.newPage()
    await navigateToProcedures(page, testPrefix)
    const listItems = await page.getByLabel("Vorgang Listenelement").all()

    for (const listItem of listItems) {
      const spanLocator = listItem.locator("span.ris-label1-regular")
      const title = await spanLocator.getAttribute("title")
      const response = await page.request.get(
        `/api/v1/caselaw/procedure?sz=10&pg=0&q=${title}`,
      )

      const responseBody = await response.json()
      const uuid = responseBody.content[0].id
      await deleteProcedure(page, uuid)
    }

    await page.reload()
    const listItemsAfterDeletion = page.getByRole("listitem")
    await expect(listItemsAfterDeletion).toHaveCount(0)
  })
})
