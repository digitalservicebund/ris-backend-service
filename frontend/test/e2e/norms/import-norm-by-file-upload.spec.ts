import { expect, test } from "@playwright/test"
import { createDataTransfer } from "../shared/e2e-utils"
import { loadJurisTestFile } from "./e2e-utils"
import { normData } from "./testdata/norm_basic"

// eslint-disable-next-line playwright/no-skipped-test
test.describe.skip("import a norm by uploading a file", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/norms/import")
  })

  test("upload Juris Zip file per file chooser", async ({ page, request }) => {
    const { filePath } = await loadJurisTestFile(
      request,
      normData.jurisZipFileName
    )

    const fileChooserEvent = page.waitForEvent("filechooser")
    await page.locator("text=oder Datei auswählen").click()
    const fileChooser = await fileChooserEvent
    await fileChooser.setFiles(filePath)

    await page.waitForURL("/norms/norm/*")

    await expect(
      page.locator(`text=${normData.officialLongTitle}`)
    ).toBeVisible()
  })

  test("it can upload Juris Zip file via drag and drop", async ({
    page,
    request,
  }) => {
    const fileName = normData.jurisZipFileName
    const { fileContent } = await loadJurisTestFile(request, fileName)
    const dataTransfer = await createDataTransfer(
      page,
      fileContent,
      fileName,
      "application/zip"
    )

    await page.dispatchEvent(".upload-drop-area", "drop", { dataTransfer })

    await page.waitForURL("/norms/norm/*")

    await expect(
      page.locator(`text=${normData.officialLongTitle}`)
    ).toBeVisible()
  })
})
