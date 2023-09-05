import { expect, Page, test } from "@playwright/test"
import { importTestData } from "./e2e-utils"
import { norm_with_structure } from "./testdata/norm_with_structure"
import { DocumentationNoGuid } from "~/e2e/norms/fixtures"

test.describe("import test data with structure and display table of contents", () => {
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
    await expect(
      page.getByText(
        [documentationElement.marker, documentationElement.heading].join(" "),
        {
          exact: true,
        },
      ),
    ).toBeVisible()

    if ("type" in documentationElement && documentationElement.documentation) {
      await checkIfDocumentationPresent(
        page,
        documentationElement.documentation,
      )
    }
  }
}
