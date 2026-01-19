import fs from "fs"
import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { getPreview } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import { navigateToSettings } from "~/e2e/caselaw/utils/e2e-utils"
import { importDocumentationUnitFromXml } from "~/e2e/caselaw/utils/importer-api-util"

test.describe("ensuring the exported XML is generated from imported decision as expected", () => {
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip(
    ({ baseURL }) => baseURL === "http://127.0.0.1",
    "Skipping this test on local execution, as there is no importer available",
  )
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("xml preview shows expected xml", async ({ page }) => {
    const apiKey = await test.step("Generate API Key", async () => {
      await navigateToSettings(page)
      const generateButton = page.getByRole("button", {
        name: "Neuen API-Schlüssel erstellen",
      })
      // eslint-disable-next-line playwright/no-conditional-in-test
      if (await generateButton.isVisible()) {
        await generateButton.click()
      }

      const copyButton = page.getByLabel(
        "API Key in die Zwischenablage kopieren",
      )
      await expect(copyButton).toBeVisible()
      return await copyButton.textContent()
    })

    const docUnitId =
      await test.step("Import the XML file for the documentation unit", async () => {
        const importResponse = await importDocumentationUnitFromXml(
          page,
          "./test/e2e/caselaw/testfiles/docunit_to_import.xml",
          apiKey!,
          process.env.IMPORTER_USERNAME!,
          process.env.IMPORTER_PASSWORD!,
          page.request,
        )

        expect(
          importResponse.ok(),
          "could not import xml file, check your credentials and importer service",
        ).toBeTruthy()

        const response = await page.request.get(
          `/api/v1/caselaw/documentunits/IITestDoc0019`,
        )
        const responseBody = await response.json()
        return responseBody.uuid
      })

    await test.step("Expect preview", async () => {
      const previewResponse = await getPreview(page, docUnitId)
      const previewXml = await previewResponse.json()

      // Import the XML file for the documentation unit
      const expectedPreview = await fs.promises.readFile(
        "./test/e2e/caselaw/testfiles/docunit_expected_preview.xml",
        "utf8",
      )
      expect(previewXml.xml).toBe(expectedPreview)
    })
  })
})
