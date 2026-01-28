import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToAttachments,
  navigateToCategories,
  save,
  uploadTestfile,
} from "~/e2e/caselaw/utils/e2e-utils"

// eslint-disable-next-line playwright/no-skipped-test
test.describe.skip(
  "base64 images are converted to attachments",
  {
    tag: ["@RISDEV-7971", "@RISDEV-3018"],
  },
  () => {
    test("Upload docx and copy image into long text", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await test.step("Upload image in attachments tab", async () => {
        await navigateToAttachments(page, prefilledDocumentUnit.documentNumber)
        await uploadTestfile(page, "sample-image-formats.docx")
        // Wait for transformed docx to be loaded to avoid NS_BINDING_ABORTED errors in firefox
        await expect(
          page.getByText("jpg:Word rotated 90 degrees"),
        ).toBeVisible()
      })

      await test.step("Navigate to categories", async () => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)
        await expect(page.getByLabel("Originaldokument anzeigen")).toBeVisible()
      })

      await test.step("Validate word image is rotated correctly", async () => {
        await navigateToCategories(page, prefilledDocumentUnit.documentNumber)
        await expect(page.getByLabel("Originaldokument anzeigen")).toBeVisible()
        const image = page
          .getByTestId("Dokumentenvorschau")
          .locator("img")
          .first()

        await expect(image).toBeVisible()

        const { naturalWidth, naturalHeight } = await image.evaluate(
          (img: HTMLImageElement) => ({
            naturalWidth: img.naturalWidth,
            naturalHeight: img.naturalHeight,
          }),
        )

        expect(naturalWidth).toBe(200)
        expect(naturalHeight).toBe(300)
        expect(naturalHeight).toBeGreaterThan(naturalWidth)
      })

      await test.step("Copy image from docx  to Orientierungssatz", async () => {
        const imgSrc = await page
          .getByTestId("Dokumentenvorschau")
          .locator("img")
          .first()
          .getAttribute("src")

        await page.evaluate((src) => {
          const container = document.querySelector(
            '[data-testid="Orientierungssatz"] div',
          )
          const img = document.createElement("img")
          img.src = src!
          container?.appendChild(img)
        }, imgSrc)
      })

      const image = page.locator(
        '[data-testid="Orientierungssatz"] img:not(.ProseMirror-separator)',
      )

      await test.step("Validate image src tag contains base64 in Orientierungssatz", async () => {
        await expect(image).toBeVisible()
        await expect(image).toHaveAttribute("src", /^data:image\/jpeg;base64,/)
      })

      await test.step("Assure image source change from base64 to api after save", async () => {
        await save(page)
        await expect(page.getByText("speichern...")).toBeHidden()

        await expect(image).toHaveAttribute(
          "src",
          /^\/api\/v1\/caselaw\/documentunits\/[^/]+\/image\/[^/]+$/,
        )
      })
    })
  },
)
