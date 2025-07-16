import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToAttachments,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
} from "~/e2e/caselaw/utils/e2e-utils"

const options = {
  skipAssert: true, // Skip assertions to test access denied.
}

test.describe("ensuring the authorization works as expected", () => {
  test("expect read access from a user of a different documentationOffice to be restricted", async ({
    documentNumber,
    pageWithBghUser,
  }) => {
    await navigateToCategories(pageWithBghUser, documentNumber, options)

    await expect(
      pageWithBghUser.getByText(
        "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await navigateToAttachments(pageWithBghUser, documentNumber, options)
    await expect(
      pageWithBghUser.getByText(
        "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await navigateToHandover(pageWithBghUser, documentNumber, options)
    await expect(
      pageWithBghUser.getByText(
        "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung",
      ),
    ).toBeVisible()
  })

  test(
    "pending proceeding preview tests",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-6109",
      },
    },
    async ({ page, pageWithBghUser }) => {
      const testCases = [
        {
          description:
            "can be accessed for users of the same documentationOffice",
          page: page,
          documentNumber: "YYTestDoc0018",
          options: { skipAssert: false, type: "pending-proceeding" },
          error: null,
        },
        {
          description: "can't be accessed for foreign users",
          page: pageWithBghUser,
          documentNumber: "YYTestDoc0018",
          options: { skipAssert: true, type: "pending-proceeding" },
          error:
            "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung",
        },
        {
          description: "can't be accessed when document number is wrong",
          page: page,
          documentNumber: "YYTestDoc9999",
          options: { skipAssert: true, type: "pending-proceeding" },
          error:
            "Diese Dokumentationseinheit existiert nicht oder Sie haben keine Berechtigung",
        },
      ]

      for (const {
        description,
        page,
        documentNumber,
        options,
        error,
      } of testCases) {
        await test.step(`Verifying: pending proceeding preview ${description}`, async () => {
          await navigateToPreview(
            page,
            documentNumber,
            options as {
              skipAssert?: boolean
              type?: "pending-proceeding" | "documentunit"
            },
          )

          await expect(page.getByText(error ?? documentNumber)).toBeVisible()
        })
      }
    },
  )
})
