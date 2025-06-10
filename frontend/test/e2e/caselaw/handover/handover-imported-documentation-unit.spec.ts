import fs from "fs"
import { expect } from "@playwright/test"
import { navigateToSettings } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { getPreview } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import { importDocumentationUnitFromXml } from "~/e2e/caselaw/utils/importer-api-util"

test.describe("ensuring the exported XML is generated from imported decision as expected", () => {
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("xml preview shows expected xml", async ({ page }) => {
    // test.skip(
    //   process.env.E2E_TEST_URL === "http://127.0.0.1",
    //   "Skipping this test on local execution",
    // )
    // generate API-Key
    await navigateToSettings(page)
    // eslint-disable-next-line playwright/no-conditional-in-test
    if (await page.getByText("gültig bis: ").isHidden()) {
      await page.getByText("Neuen API-Schlüssel erstellen").click()
    }
    const apiKey = await page
      .getByTitle("API Key in die Zwischenablage kopieren")
      .textContent()

    // Import the XML file for the documentation unit
    const importResponse = await importDocumentationUnitFromXml(
      page,
      "./test/e2e/caselaw/testfiles/docunit_to_import.xml",
      apiKey!,
      process.env.IMPORTER_USERNAME ?? "importer",
      process.env.IMPORTER_PASSWORD ?? "importer",
      page.request,
    )

    expect(importResponse.ok()).toBeTruthy()

    const response = await page.request.get(
      `/api/v1/caselaw/documentunits/YYTestDoc0019`,
    )
    const responseBody = await response.json()
    const uuid = responseBody.uuid

    const previewResponse = await getPreview(page, uuid)
    const previewXml = await previewResponse.json()

    // Import the XML file for the documentation unit
    const expectedPreview = await fs.promises.readFile(
      "./test/e2e/caselaw/testfiles/docunit_expected_preview.xml",
      "utf8",
    )
    expect(previewXml.xml).toBe(expectedPreview)
  })
})
