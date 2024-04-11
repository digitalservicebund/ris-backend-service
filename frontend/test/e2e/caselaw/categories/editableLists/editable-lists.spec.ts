/* eslint-disable playwright/no-conditional-in-test */
/* eslint-disable playwright/no-conditional-expect */
import { expect } from "@playwright/test"
import SingleNorm from "@/domain/singleNorm"
import {
  fillPreviousDecisionInputs,
  fillActiveCitationInputs,
  fillEnsuingDecisionInputs,
  navigateToCategories,
  publishDocumentationUnit,
  waitForSaving,
  fillNormInputs,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("related documentation units", () => {
  test("renders empty list item in creation mode, when none in list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          //adding, deleting and cancel editing of empty item not possible
          if (container === normsContainer) {
            await expect(
              container.getByLabel(`${containerLabel} speichern`),
            ).toBeHidden()
          } else
            await expect(
              container.getByLabel(`${containerLabel} speichern`),
            ).toBeDisabled()

          await expect(container.getByLabel("Abbrechen")).toBeHidden()

          await expect(container.getByLabel("Löschen")).toBeHidden()
        },
      )
    }
  })

  test("added items are saved, can be edited and deleted", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
    ]

    for (const container of containers) {
      const section = await container.first().getAttribute("aria-label")
      const fileNumber1 = generateString()
      const fileNumber2 = generateString()
      const fileNumber3 = generateString()

      await test.step("for category " + section, async () => {
        // adding empty entry not possible
        await expect(page.getByLabel(section + " speichern")).toBeDisabled()

        // add entry
        await container
          .getByLabel("Aktenzeichen " + section, { exact: true })
          .fill(fileNumber1)

        await container.getByLabel(section + " speichern").click()
        await expect(container.getByText(fileNumber1)).toBeVisible()

        // edit entry
        await container.getByLabel("Listen Eintrag").first().click()
        await container
          .getByLabel("Aktenzeichen " + section, { exact: true })
          .fill(fileNumber2)

        await container.getByLabel(section + " speichern").click()
        await expect(container.getByText(fileNumber1)).toBeHidden()
        await expect(container.getByText(fileNumber2)).toBeVisible()

        // the second list item is a default list entry
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

        // add second entry
        await waitForSaving(
          async () => {
            await container
              .getByLabel("Aktenzeichen " + section, { exact: true })
              .fill(fileNumber3)
            await container.getByLabel(section + " speichern").click()
          },
          page,
          { clickSaveButton: true },
        )

        // the third list item is a default list entry
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(3)
        await page.reload()
        // the default list entry is not shown on reload page
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
        await container.getByLabel("Weitere Angabe").isVisible()

        const listEntries = container.getByLabel("Listen Eintrag")
        await expect(listEntries).toHaveCount(2)
        await listEntries.first().click()
        await container.getByLabel("Eintrag löschen").click()
        // the default list entry is not shown on delete item
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
        await container.getByLabel("Weitere Angabe").isVisible()
      })
    }
  })

  test("validates list item against required fields", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await publishDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const inputs = {
            court: prefilledDocumentUnit.coreData.court?.label,
            fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
            documentType: prefilledDocumentUnit.coreData.documentType?.label,
            decisionDate: "31.12.2019",
          }
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          const fileNumber = generateString()

          await container
            .getByLabel("Aktenzeichen " + containerLabel, { exact: true })
            .fill(fileNumber)

          await page.getByLabel(`${containerLabel} speichern`).click()

          await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeVisible()
          await page
            .getByLabel(containerLabel, { exact: true })
            .getByLabel("Listen Eintrag")
            .first()
            .click()
          if (container === activeCitationContainer) {
            await expect(
              page
                .getByLabel(containerLabel)
                .getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(3)
            await fillActiveCitationInputs(page, {
              ...inputs,
              citationType: "Änderung",
            })
          } else if (container === previousDecisionContainer) {
            await expect(
              page
                .getByLabel(containerLabel)
                .getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(2)
            await fillPreviousDecisionInputs(page, inputs)
          } else if (container === ensuingDecisionContainer) {
            await expect(
              page
                .getByLabel(containerLabel)
                .getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(2)
            await fillEnsuingDecisionInputs(page, inputs)
          }

          await page.getByLabel(`${containerLabel} speichern`).click()

          await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeHidden()
        },
      )
    }
  })

  test("after first save of list item, cancel and delete buttons are visible", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const fileNumber = generateString()
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { fileNumber: fileNumber })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { fileNumber: fileNumber })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { fileNumber: fileNumber })
          }

          if (container === normsContainer) {
            await fillNormInputs(page, {
              normAbbreviation: "PBefG",
            })
          }

          await expect(container.getByLabel("Abbrechen")).toBeHidden()

          await expect(
            container.getByLabel("Löschen", { exact: true }),
          ).toBeHidden()

          await container.getByLabel(`${containerLabel} speichern`).click()

          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

          await container.getByLabel("Listen Eintrag").first().click()
          await expect(container.getByLabel("Abbrechen")).toBeVisible()

          await expect(
            container.getByLabel("Eintrag löschen", { exact: true }),
          ).toBeVisible()
        },
      )
    }
  })

  test("deleting behaviour of list items", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          await navigateToCategories(page, documentNumber)

          //list item 1
          const fileNumber1 = generateString()
          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, {
              fileNumber: fileNumber1,
            })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, {
              fileNumber: fileNumber1,
            })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, {
              fileNumber: fileNumber1,
            })
          }
          if (container === normsContainer) {
            await fillNormInputs(page, {
              normAbbreviation: "PBefG",
            })
          }
          await container.getByLabel(`${containerLabel} speichern`).click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

          //list item 2
          const fileNumber2 = generateString()
          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, {
              fileNumber: fileNumber2,
            })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, {
              fileNumber: fileNumber2,
            })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, {
              fileNumber: fileNumber2,
            })
          }
          if (container === normsContainer) {
            await fillNormInputs(page, {
              normAbbreviation: "AusstgBeschWoEigGVV",
            })
          }
          await container.getByLabel(`${containerLabel} speichern`).click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(3)

          // leaving an empty list item, deletes it
          await container.getByLabel("Listen Eintrag").nth(1).click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

          await container.getByLabel("Listen Eintrag").last().click()
          await container.getByLabel("Eintrag löschen").click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

          //deleting resets edit mode
          await expect(container.getByLabel("Weitere Angabe")).toBeVisible()

          //deleting last list item, adds a new default item
          await container.getByLabel("Listen Eintrag").first().click()
          await container.getByLabel("Eintrag löschen").click()

          await expect(container.getByLabel("Abbrechen")).toBeHidden()
          await expect(container.getByLabel("Eintrag löschen")).toBeHidden()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

          // resets validation errors
          await expect(
            container.getByText("Pflichtfeld nicht befüllt"),
          ).toHaveCount(0)
        },
      )
    }
  })

  test("cancel editing list item does not update values", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          const firstEntry = container === normsContainer ? "PBefG" : "1234"
          const secondEntry =
            container === normsContainer ? "KaffeeStG" : "4321"
          await navigateToCategories(page, documentNumber)

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { fileNumber: firstEntry })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { fileNumber: firstEntry })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { fileNumber: firstEntry })
          }

          if (container === normsContainer) {
            await fillNormInputs(page, {
              normAbbreviation: firstEntry,
            })
          }

          await expect(container.getByLabel("Abbrechen")).toBeHidden()

          await container.getByLabel(`${containerLabel} speichern`).click()

          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

          await container.getByLabel("Listen Eintrag").first().click()

          await expect(container.getByLabel("Abbrechen")).toBeVisible()

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, {
              fileNumber: secondEntry,
            })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, {
              fileNumber: secondEntry,
            })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, {
              fileNumber: secondEntry,
            })
          }

          if (container === normsContainer) {
            await fillNormInputs(page, {
              normAbbreviation: secondEntry,
            })
          }

          await container.getByLabel("Abbrechen").click()
          await expect(container.getByText(firstEntry)).toBeVisible()
          await expect(container.getByText(secondEntry)).toBeHidden()
        },
      )
    }
  })

  test("add new item only possible when no item in edit mode", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          const fileNumber = generateString()
          await navigateToCategories(page, documentNumber)

          await expect(container.getByLabel("Weitere Angabe")).toBeHidden()

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { fileNumber: fileNumber })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { fileNumber: fileNumber })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { fileNumber: fileNumber })
          }
          if (container === normsContainer) {
            await fillNormInputs(page, {
              normAbbreviation: "PBefG",
            })
          }

          await container.getByLabel(`${containerLabel} speichern`).click()

          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

          await container.getByLabel("Listen Eintrag").first().click()

          await expect(container.getByLabel("Weitere Angabe")).toBeHidden()
        },
      )
    }
  })

  test("incomplete date input shows error message and does not persist", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          await navigateToCategories(page, documentNumber)

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { decisionDate: "03" })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { decisionDate: "03" })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { decisionDate: "03" })
          }
          if (container === normsContainer) {
            await fillEnsuingDecisionInputs(page, { decisionDate: "03" })
          }
          await fillNormInputs(page, {
            normAbbreviation: "PBefG",
            singleNorms: [{ dateOfVersion: "03" } as SingleNorm],
          })

          await page.keyboard.press("Tab")

          await expect(
            container.locator("text=Unvollständiges Datum"),
          ).toBeVisible()
        },
      )
    }
  })
})
