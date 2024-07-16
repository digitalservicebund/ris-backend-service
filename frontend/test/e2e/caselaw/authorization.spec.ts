import { expect } from "@playwright/test"
import { caselawTest as test } from "./fixtures"

test.describe("ensuring the handover of documentunits works as expected", () => {
  test("expect read access from a user of a different documentationOffice to be restricted", async ({
    documentNumber,
    pageWithBghUser,
  }) => {
    await pageWithBghUser.goto(
      `/caselaw/documentunit/${documentNumber}/categories`,
    )
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await pageWithBghUser.goto(`/caselaw/documentunit/${documentNumber}/files`)
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await pageWithBghUser.goto(
      `/caselaw/documentunit/${documentNumber}/handover`,
    )
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()
  })
})
