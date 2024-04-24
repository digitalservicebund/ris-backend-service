import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToFiles,
  uploadTestfile,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe("test attachement panel", () => {
  test.beforeEach(async ({ page, documentNumber }) => {
    await navigateToFiles(page, documentNumber)
    await uploadTestfile(page, "sample.docx")
  })

  test("attachment panel is shown when doucment is uploaded", async ({
    page,
  }) => {
    const attachmentView = page.locator("#attachment-view")
    await expect(attachmentView).toBeVisible()
    await expect(page).toHaveURL(/showAttachmentPanel=true/)
  })

  test("attachment panel state is passed to catagories", async ({
    page,
    documentNumber,
  }) => {
    const attachmentView = page.locator("#attachment-view")
    await expect(attachmentView).toBeVisible()

    await navigateToCategories(page, documentNumber)
    await expect(page).toHaveURL(/showAttachmentPanel=true/)
    await expect(attachmentView).toBeVisible()
  })

  test("attachment panel state changes the route params", async ({ page }) => {
    const attachmentView = page.locator("#attachment-view")
    await expect(attachmentView).toBeVisible()

    await page.getByLabel("Dokumentansicht schließen").click()
    await expect(page).toHaveURL(/showAttachmentPanel=false/)

    await page.getByLabel("Dokumentansicht öffnen").click()
    await expect(page).toHaveURL(/showAttachmentPanel=true/)
  })
})
