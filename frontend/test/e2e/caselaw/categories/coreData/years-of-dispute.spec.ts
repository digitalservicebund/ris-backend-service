import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToPreview,
  navigateToPublication,
  publishDocumentationUnit,
  waitForSaving,
} from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

test.describe(
  "years of dispute",
  {
    annotation: {
      type: "epic",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-3918",
    },
  },
  () => {
    test(
      "display, adding, navigating, deleting multiple years of dispute",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4197",
        },
      },
      async ({ page, documentNumber }) => {
        await navigateToCategories(page, documentNumber)

        await waitForSaving(
          async () => {
            await test.step("Add three years of dispute, check they are visible", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("2020")
              await page.keyboard.press("Enter")
              await page.locator("[aria-label='Streitjahr']").fill("2021")
              await page.keyboard.press("Enter")

              await page.locator("[aria-label='Streitjahr']").fill("2022")
              await page.keyboard.press("Enter")

              await expect(page.getByText("2020")).toBeVisible()
              await expect(page.getByText("2021")).toBeVisible()
              await expect(page.getByText("2022")).toBeVisible()
            })

            await test.step("Navigate back by arrow left, delete last chip on enter", async () => {
              await page.keyboard.press("ArrowLeft")
              await page.keyboard.press("Enter")

              await expect(page.getByText("2022")).toBeHidden()
            })

            await test.step("Tab out, tab in, navigate back by arrow left, delete last chip on enter", async () => {
              await page.keyboard.press("Tab")
              await page.keyboard.press("Tab")
              await page.keyboard.down("Shift")
              await page.keyboard.press("Tab")

              await page.keyboard.press("ArrowLeft")
              await page.keyboard.press("Enter")
              await expect(page.getByText("2021")).toBeHidden()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await test.step("Check if years of dispute are persisted in reload", async () => {
          await page.reload()
          await expect(page.getByText("2020")).toBeVisible()
        })
      },
    )

    test(
      "years of dispute visible in preview",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4198",
        },
      },
      async ({ page, documentNumber }) => {
        await navigateToCategories(page, documentNumber)

        await waitForSaving(
          async () => {
            await test.step("Add two years of dispute, check they are visible", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("2020")
              await page.keyboard.press("Enter")
              await page.locator("[aria-label='Streitjahr']").fill("2021")
              await page.keyboard.press("Enter")

              await expect(page.getByText("2020")).toBeVisible()
              await expect(page.getByText("2021")).toBeVisible()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await test.step("Navigate to preview, check they are visible", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("2020")).toBeVisible()
          await expect(page.getByText("2021")).toBeVisible()
        })
      },
    )

    test(
      "years of dispute are exported",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4199",
        },
      },
      async ({ page, prefilledDocumentUnit }) => {
        await publishDocumentationUnit(
          page,
          prefilledDocumentUnit.documentNumber || "",
        )

        await navigateToCategories(
          page,
          prefilledDocumentUnit.documentNumber || "",
        )

        await waitForSaving(
          async () => {
            await test.step("Add two years of dispute, check they are visible", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("2020")
              await page.keyboard.press("Enter")
              await page.locator("[aria-label='Streitjahr']").fill("2021")
              await page.keyboard.press("Enter")

              await expect(page.getByText("2020")).toBeVisible()
              await expect(page.getByText("2021")).toBeVisible()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await test.step("Navigate to publication, click in 'XML-Vorschau', check they are visible", async () => {
          await navigateToPublication(
            page,
            prefilledDocumentUnit.documentNumber!,
          )
          await expect(
            page.getByText("XML Vorschau der Veröffentlichung"),
          ).toBeVisible()
          await page.getByText("XML Vorschau der Veröffentlichung").click()
          await expect(
            page.getByText("<streitjahr>2020</streitjahr>"),
          ).toBeVisible()
          await expect(
            page.getByText("<streitjahr>2021</streitjahr>"),
          ).toBeVisible()
        })
      },
    )

    test(
      "validating years of dispute input",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4200",
        },
      },
      async ({ page, documentNumber }) => {
        await navigateToCategories(page, documentNumber)

        await waitForSaving(
          async () => {
            await test.step("Add two identical years of dispute not possible, shows error", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("2022")
              await page.keyboard.press("Enter")
              await expect(page.getByText("2022")).toBeVisible()

              await page.locator("[aria-label='Streitjahr']").fill("2022")
              await page.keyboard.press("Enter")
              await expect(
                page.getByText("2022 bereits vorhanden"),
              ).toBeVisible()
            })

            await test.step("Add invalid years of dispute not possible, former error replaced by new one", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("999")
              await page.keyboard.press("Enter")
              await expect(
                page.getByText("2022 bereits vorhanden"),
              ).toBeHidden()
              await expect(page.getByText("Kein valides Jahr")).toBeVisible()
            })

            await test.step("Add years of dispute in future not possible, former error replaced by new one", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("2030")
              await page.keyboard.press("Enter")
              await expect(page.getByText("Kein valides Jahr")).toBeHidden()
              await expect(
                page.getByText("Streitjahr darf nicht in der Zukunft liegen"),
              ).toBeVisible()
            })

            await test.step("On blur validates input, input is not saved with error", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("20")
              await page.keyboard.press("Tab")
              await expect(
                page.getByText("Streitjahr darf nicht in der Zukunft liegen"),
              ).toBeHidden()
              await expect(page.getByText("Kein valides Jahr")).toBeVisible()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await test.step("Check if onyl valids years of dispute are persisted in reload", async () => {
          await page.reload()
          await expect(page.getByText("2022")).toBeVisible()
        })
      },
    )
  },
)
