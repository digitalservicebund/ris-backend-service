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

        const testData = [
          "1850",
          "1950",
          "1989",
          "1990",
          "1995",
          "2000",
          "2005",
          "2010",
          "2011",
          "2015",
          "2018",
          "2020",
          "2021",
          "2023",
          "2024",
        ]

        await waitForSaving(
          async () => {
            for (const year of testData) {
              await test.step(
                "Add year " + year + ", press enter, check for visibility",
                async () => {
                  await page.locator("[aria-label='Streitjahr']").fill(year)
                  await page.keyboard.press("Enter")

                  await expect(page.getByText(year)).toBeVisible()
                },
              )
            }

            await test.step("Expect 15 years of dispute to be visible in same order", async () => {
              const chips = await page.getByTestId("chip").all()
              expect(chips.length).toBe(15)
              for (let i = 0; i < chips.length; i++) {
                const chipValue = chips[i].getByTestId("chip-value")
                await expect(chipValue).toHaveText(testData[i])
              }
            })

            await test.step("Navigate back by arrow left, delete last chip on enter", async () => {
              await expect(page.getByText("2024")).toBeVisible()
              await page.keyboard.press("ArrowLeft")
              await page.keyboard.press("Enter")

              await expect(page.getByText("2024")).toBeHidden()
            })

            await test.step("Tab out, tab in, navigate back by arrow left, delete last chip on enter", async () => {
              await expect(page.getByText("2023")).toBeVisible()
              await page.keyboard.press("Tab")
              await page.keyboard.press("Tab")
              await page.keyboard.down("Shift")
              await page.keyboard.press("Tab")

              await page.keyboard.press("ArrowLeft")
              await page.keyboard.press("Enter")
              await expect(page.getByText("2023")).toBeHidden()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await test.step("Add deleted years again, check if testdata persists on reload", async () => {
          await waitForSaving(
            async () => {
              await page.locator("[aria-label='Streitjahr']").fill("2023")
              await page.keyboard.press("Enter")

              await page.locator("[aria-label='Streitjahr']").fill("2024")
              await page.keyboard.press("Enter")
            },
            page,
            { clickSaveButton: true },
          )

          await page.reload()
          for (const year of testData) {
            await expect(page.getByText(year)).toBeVisible()
          }
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
        const testData = ["2020", "2021", "2022"]

        await waitForSaving(
          async () => {
            for (const year of testData) {
              await test.step(
                "Add year " + year + ", press enter, check for visibility",
                async () => {
                  await page.locator("[aria-label='Streitjahr']").fill(year)
                  await page.keyboard.press("Enter")

                  await expect(page.getByText(year)).toBeVisible()
                },
              )
            }
          },
          page,
          { clickSaveButton: true },
        )
        await test.step("Expect all three years to be visible in preview", async () => {
          await navigateToPreview(page, documentNumber)
          for (const year of testData) {
            await expect(page.getByText(year)).toBeVisible()
          }
        })

        await test.step("Remove all years of input, check that category in preview is not visible anymore", async () => {
          await navigateToCategories(page, documentNumber)
          await waitForSaving(
            async () => {
              for (let i = 0; i < testData.length; i++) {
                await page.getByLabel("Löschen").first().click()
              }
            },
            page,
            { clickSaveButton: true },
          )
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("Streitjahr")).toBeHidden()
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
          prefilledDocumentUnit.documentNumber ?? "",
        )

        await navigateToCategories(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        const testData = ["2020", "2021"]

        await waitForSaving(
          async () => {
            for (const year of testData) {
              await test.step(
                "Add year " + year + ", press enter, check for visibility",
                async () => {
                  await page.locator("[aria-label='Streitjahr']").fill(year)
                  await page.keyboard.press("Enter")

                  await expect(page.getByText(year)).toBeVisible()
                },
              )
            }
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

          const nodes = await page
            .locator('code:has-text("<streitjahr>")')
            .all()

          for (let i = 0; i < nodes.length; i++) {
            const nodeText = await nodes[i].textContent()
            const match = nodeText?.match(/<streitjahr>(.*?)<\/streitjahr>/)
            // eslint-disable-next-line playwright/no-conditional-in-test
            const extractedValue = match ? match[1] : null
            expect(extractedValue).toBe(testData[i])
          }
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

            await test.step("Add more then 4 numbers not possible", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("20202")
              await page.keyboard.press("Enter")
              await expect(page.getByText("2020")).toBeVisible()
            })

            await test.step("Add characters not possible", async () => {
              await page.locator("[aria-label='Streitjahr']").fill("abcd")
              await page.keyboard.press("Enter")
              await expect(page.getByText("abcd")).toBeHidden()
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
