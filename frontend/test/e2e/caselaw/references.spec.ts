import { expect } from "@playwright/test"
import {
  navigateToPreview,
  navigateToHandover,
  waitForSaving,
  navigateToReferences,
  fillInput,
  waitForInputValue,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe(
  "references",
  {
    annotation: {
      type: "epic",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4264",
    },
  },
  () => {
    test(
      "Display, adding, editing and deleting multiple references",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4336",
        },
      },
      async ({ page, documentNumber }) => {
        await test.step("References is a new selectable menu item in a documentation unit ", async () => {
          await navigateToReferences(page, documentNumber)
        })

        await waitForSaving(
          async () => {
            await test.step("When typing the legal periodical abbreviation (MM), the entry including abbreviation, title and subtitle can be found in the combobox", async () => {
              await fillInput(page, "Periodikum", "MM")
              await expect(
                page.getByText("Magazin des Berliner Mieterverein e.V.", {
                  exact: true,
                }),
              ).toBeVisible()
              await page
                .getByText("MM | Mieter Magazin", { exact: true })
                .click()
              await waitForInputValue(
                page,
                "[aria-label='Periodikum']",
                "MM | Mieter Magazin",
              )
            })

            await test.step("When typing the incomplete legal periodical title (Mieter Magaz), the entry including abbreviation, title and subtitle can be found in the combobox", async () => {
              ;async () => {
                await fillInput(page, "Periodikum", "Mieter Magaz")
                await expect(
                  page.getByText("Magazin des Berliner Mieterverein e.V.", {
                    exact: true,
                  }),
                ).toBeVisible()
                await page
                  .getByText("MM | Mieter Magazin", { exact: true })
                  .click()
                await waitForInputValue(
                  page,
                  "[aria-label='Periodikum']",
                  "MM | Mieter Magazin",
                )
              }
            })

            await test.step("citation shows citation example", async () => {
              await expect(
                page.getByText("Zitierbeispiel: 2011, Nr 6, 22-23"),
              ).toBeVisible()
            })

            await test.step("citation and supplement can be added", async () => {
              await fillInput(page, "Zitatstelle", "2024, Nr 1, 2-5")
              await fillInput(page, "Klammernzusatz", "LT")
            })

            await test.step("Reference can be added to editable list", async () => {
              await page.locator("[aria-label='Übernehmen']").click()
              await expect(
                page.getByText("MM, 2ß24, Nr. 1 2-5 (LT) sekundär"),
              ).toBeVisible()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await test.step("Reference is persisted and shown after reload", async () => {
          await page.reload()
          await expect(
            page.getByText("MM, 2ß24, Nr. 1 2-5 (LT) sekundär"),
          ).toBeVisible()
        })

        await waitForSaving(
          async () => {
            await test.step("Edit legal periodical in reference, verify that it is updated in the list", async () => {
              await page
                .getByText("MM - Mieter Magazin, 2ß24, Nr. 1 2-5 (LT) sekundär")
                .click()
              await fillInput(page, "Periodikum", "GVBl BB")
              await page
                .getByText(
                  "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
                  { exact: true },
                )
                .click()
              await waitForInputValue(
                page,
                "[aria-label='Periodikum']",
                "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
              )
              await expect(
                page.getByText("Zitierbeispiel: 1991, 676-681"),
              ).toBeVisible()
              await page
                .getByText("GVBl BB, 2ß24, Nr. 1 2-5 (LT) amtlich")
                .click()
            })

            await test.step("Verify correct rendering in list when removing citation and changing supplement", async () => {
              ;async () => {
                await fillInput(page, "Zitatstelle", "")
                await fillInput(page, "Klammernzusatz", "LT")
                await page.locator("[aria-label='Übernehmen']").click()
                await expect(
                  page.getByText("GVBl BB, (LT) amtlich"),
                ).toBeVisible()
              }
            })
          },
          page,
          { clickSaveButton: true },
        )

        await waitForSaving(
          async () => {
            await test.step("Add second reference without supplement, verify that it shown in the list", async () => {
              await page.locator("[aria-label='Weitere Angabe']").click()
              await fillInput(page, "Periodikum", "wdg")
              await page
                .getByText("WdG | Welt der Gesundheitsversorgung", {
                  exact: true,
                })
                .click()
              await waitForInputValue(
                page,
                "[aria-label='Periodikum']",
                "WdG | Welt der Gesundheitsversorgung",
              )
              await fillInput(page, "Zitatstelle", "2024, 10-12")
              await page.locator("[aria-label='Übernehmen']").click()
              await expect(
                page.getByText("WdG, 2024, 10-12 sekundär"),
              ).toBeVisible()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await waitForSaving(
          async () => {
            await test.step("Delete references and verify they disappear from the list", async () => {
              await page.getByText("WdG, 2024, 10-12 sekundär").click()
              await page.locator("[aria-label='Eintrag Löschen']").click()
              await expect(
                page.getByText("WdG, 2024, 10-12 sekundär"),
              ).toBeHidden()
              await page.getByText("GVBl BB, (LT) amtlich").click()
              await page.locator("[aria-label='Eintrag Löschen']").click()
              await expect(page.getByText("GVBl BB, (LT) amtlich")).toBeHidden()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await page.reload()

        await expect(page.getByLabel("Listen Eintrag")).not.toBeAttached()
      },
    )

    test(
      "References are visible in preview",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4335",
        },
      },
      async ({ page, documentNumber }) => {
        await test.step("References are not rendered in preview when empty", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("Fundstellen")).toBeHidden()
        })

        await navigateToReferences(page, documentNumber)

        await waitForSaving(
          async () => {
            await test.step("Add primary and secondary references with all data, verify remdering in preview", async () => {
              await page.locator("[aria-label='Weitere Angabe']").click()
              await fillInput(page, "Periodikum", "wdg")
              await page
                .getByText("WdG | Welt der Gesundheitsversorgung", {
                  exact: true,
                })
                .click()
              await fillInput(page, "Zitatstelle", "2024, 10-12")
              await fillInput(page, "Klammernzusatz", "LT")
              await page.locator("[aria-label='Übernehmen']").click()
              await expect(
                page.getByText("WdG, 2024, 10-12 (LT) sekundär"),
              ).toBeVisible()

              await page.locator("[aria-label='Weitere Angabe']").click()
              await fillInput(page, "Periodikum", "GVBl BB")
              await page
                .getByText(
                  "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
                  { exact: true },
                )
                .click()
              await fillInput(page, "Zitatstelle", "2020, 01-99")
              await fillInput(page, "Klammernzusatz", "L")
              await page.locator("[aria-label='Übernehmen']").click()
              await expect(
                page.getByText("GVBl BB, 2020, 01-99 (L) amtlich"),
              ).toBeVisible()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await navigateToPreview(page, documentNumber)

        await expect(
          page.getByText("Amtliche FundstellenGVBl BB - 2020, 01-99 (L)"),
        ).toBeVisible()
        await expect(
          page.getByText("Sekundäre FundstellenMM- 2024, 10-12 (LT)"),
        ).toBeVisible()
      },
    )

    test(
      "References can be exported",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4337",
        },
      },
      async ({ page, prefilledDocumentUnit }) => {
        await navigateToReferences(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        await waitForSaving(
          async () => {
            await test.step("Add primary and secondary references with all data", async () => {
              await page.locator("[aria-label='Weitere Angabe']").click()
              await fillInput(page, "Periodikum", "wdg")
              await page
                .getByText("WdG | Welt der Gesundheitsversorgung", {
                  exact: true,
                })
                .click()
              await fillInput(page, "Zitatstelle", "2024, 10-12")
              await fillInput(page, "Klammernzusatz", "LT")
              await page.locator("[aria-label='Übernehmen']").click()
              await expect(
                page.getByText("WdG, 2024, 10-12 (LT) sekundär"),
              ).toBeVisible()

              await page.locator("[aria-label='Weitere Angabe']").click()
              await fillInput(page, "Periodikum", "GVBl BB")
              await page
                .getByText(
                  "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
                  { exact: true },
                )
                .click()
              await fillInput(page, "Zitatstelle", "2020, 01-99")
              await fillInput(page, "Klammernzusatz", "L")
              await page.locator("[aria-label='Übernehmen']").click()
              await expect(
                page.getByText("GVBl BB, 2020, 01-99 (L) amtlich"),
              ).toBeVisible()
            })
          },
          page,
          { clickSaveButton: true },
        )

        await test.step("Navigate to handover, click in 'XML-Vorschau', check references are visible", async () => {
          await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
          await expect(page.getByText("XML Vorschau")).toBeVisible()
          await page.getByText("XML Vorschau").click()

          const primaryNodes = await page
            .locator('code:has-text("<fundstelle typ="amtlich">")')
            .all()
          expect(primaryNodes.length).toBe(1)

          const secondaryNodes = await page
            .locator('code:has-text("<fundstelle typ="nichtamtlich">")')
            .all()
          expect(secondaryNodes.length).toBe(1)

          const nodeText = await primaryNodes[0].textContent()
          const primaryPeriodical = nodeText?.match(
            /<periodikum>(.*?)<\/periodikum>/,
          )
          // eslint-disable-next-line playwright/no-conditional-in-test
          const extractedValue = primaryPeriodical ? primaryPeriodical[1] : null
          expect(extractedValue).toBe("GVBl BB")

          const primaryCitation = nodeText?.match(
            /<zitstelle>(.*?)<\/zitstelle>/,
          )
          // eslint-disable-next-line playwright/no-conditional-in-test
          const extractepPrimaryCitation = primaryCitation
            ? primaryCitation[1]
            : null
          expect(extractepPrimaryCitation).toBe("2020, 01-99 (L)")

          const secondaryNodeText = await secondaryNodes[0].textContent()
          const secondaryPeriodical = secondaryNodeText?.match(
            /<periodikum>(.*?)<\/periodikum>/,
          )
          // eslint-disable-next-line playwright/no-conditional-in-test
          const extractedSecondaryValue = secondaryPeriodical
            ? secondaryPeriodical[1]
            : null
          expect(extractedSecondaryValue).toBe("WdG")

          const secondaryCitation = secondaryNodeText?.match(
            /<zitstelle>(.*?)<\/zitstelle>/,
          )
          // eslint-disable-next-line playwright/no-conditional-in-test
          const extractepSecondaryCitation = secondaryCitation
            ? secondaryCitation[1]
            : null
          expect(extractepSecondaryCitation).toBe("2024, 10-12 (LT)")
        })
      },
    )
  },
)
