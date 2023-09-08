import { expect, Page, test } from "@playwright/test"
import { importTestData } from "./e2e-utils"
import { norm_with_structure } from "./testdata/norm_with_structure"
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
  })
})

async function checkIfDocumentationPresent(
  page: Page,
  documentation?: DocumentationNoGuid[],
) {
  for (const documentationElement of documentation
    ? Object.values(documentation)
    : []) {
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
