import { expect } from "@playwright/test"
import { caselawTest as test } from "./fixtures"
import {
  navigateToAttachments,
  navigateToCategories,
  navigateToHandover,
} from "~/e2e/caselaw/e2e-utils"

const options = {
  skipAssert: true, // Skip assertions to test access denied.
}

test.describe("ensuring the handover of documentunits works as expected", () => {
  test("expect read access from a user of a different documentationOffice to be restricted", async ({
    documentNumber,
    pageWithBghUser,
  }) => {
    await navigateToCategories(pageWithBghUser, documentNumber, options)

    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await navigateToAttachments(pageWithBghUser, documentNumber, options)
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await navigateToHandover(pageWithBghUser, documentNumber, options)
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()
  })
})
