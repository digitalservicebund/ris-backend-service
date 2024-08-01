import { expect } from "@playwright/test"
import { navigateToAttachments, uploadTestfile } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

test.describe(
  "metadata is extracted from uploaded docx files",
  {
    annotation: {
      type: "epic",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4423",
    },
  },
  () => {
    test(
      "upload file with metadata into empty document unit should add all properties",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4423",
        },
      },
      async ({ page, documentNumber }) => {
        await navigateToAttachments(page, documentNumber)

        await test.step("upload file with Aktenzeichen, Rechtskraft, Gericht and Spruchkoerper metadata", async () => {
          await uploadTestfile(page, "with_metadata.docx")
          await expect(page.getByText("Hochgeladen am")).toBeVisible()
        })

        await test.step("open preview in sidepanel and check if metadata is filled", async () => {
          await page.getByLabel("Vorschau anzeigen").click()
          await expect(page.getByText("AktenzeichenII B 29/24")).toBeVisible()
          await expect(page.getByText("RechtskraftJa")).toBeVisible()
          await expect(page.getByText("GerichtBFH")).toBeVisible()
          await expect(page.getByText("Spruchkörper2. Senat")).toBeVisible()
        })
      },
    )
  },
)
