import { expect, Page, test } from "@playwright/test"
import { importTestData } from "./e2e-utils"
import { norm_with_structure } from "./testdata/norm_with_structure"
import { Conclusion, Formula, Recitals } from "@/domain/norm"
import { DocumentationNoGuid } from "~/e2e/norms/fixtures"

test.describe("display table of contents and navigate to documentation view page", () => {
  test("all structure elements and articles are expanded", async ({
    page,
    request,
  }) => {
    const { guid } = await importTestData(request, norm_with_structure)
    await page.goto(`/norms/norm/${guid}/content`)

    await expect(
      page.getByText("Nichtamtliches Inhaltsverzeichnis"),
    ).toBeVisible()

    await checkIfDocumentationPresent(page, norm_with_structure.documentation)
    await checkIfFormulaPresent(page, norm_with_structure.formula)
    await checkIfRecitalsPresent(page, norm_with_structure.recitals)
    await checkIfConclusionPresent(page, norm_with_structure.conclusion)
  })
})

async function checkIfDocumentationPresent(
  page: Page,
  documentation?: DocumentationNoGuid[],
) {
  for (const documentationElement of documentation ?? []) {
    const linkLocator = page.getByText(
      [documentationElement.marker, documentationElement.heading].join(" "),
      {
        exact: true,
      },
    )
    await expect(linkLocator).toBeVisible()
    await linkLocator.click()
    await page.waitForSelector("input")

    const nameInputLocator = page.getByLabel("Bezeichnung des Elements")
    const nameInputValue = await nameInputLocator.inputValue()
    expect(nameInputValue).toBe(documentationElement.marker)

    const headingInputLocator = page.getByLabel("Überschrift des Elements")
    const headingInputValue = await headingInputLocator.inputValue()
    expect(headingInputValue).toBe(documentationElement.heading)

    await page.goBack()

    if ("type" in documentationElement && documentationElement.documentation) {
      await checkIfDocumentationPresent(
        page,
        documentationElement.documentation,
      )
    }
  }
}

async function checkIfFormulaPresent(page: Page, formula?: Formula) {
  const linkLocator = page.getByText("Eingangsformel", { exact: true })
  await expect(linkLocator).toBeVisible()
  await linkLocator.click()
  await page.waitForSelector("input")

  const nameInputLocator = page.getByRole("textbox", { name: "Text" })
  const nameInputValue = await nameInputLocator.inputValue()
  expect(nameInputValue).toBe(formula?.text)

  await page.goBack()
}

async function checkIfRecitalsPresent(page: Page, recitals?: Recitals) {
  const linkLocator = page.getByText("Präambel", { exact: true })
  await expect(linkLocator).toBeVisible()
  await linkLocator.click()
  await page.waitForSelector("input")

  const nameInputLocator = page.getByRole("textbox", { name: "Text" })
  const nameInputValue = await nameInputLocator.inputValue()
  expect(nameInputValue).toBe(recitals?.text)

  await page.goBack()
}

async function checkIfConclusionPresent(page: Page, conclusion?: Conclusion) {
  const linkLocator = page.getByText("Schlussformel", { exact: true })
  await expect(linkLocator).toBeVisible()
  await linkLocator.click()
  await page.waitForSelector("input")

  const nameInputLocator = page.getByRole("textbox", { name: "Text" })
  const nameInputValue = await nameInputLocator.inputValue()
  expect(nameInputValue).toBe(conclusion?.text)

  await page.goBack()
}
