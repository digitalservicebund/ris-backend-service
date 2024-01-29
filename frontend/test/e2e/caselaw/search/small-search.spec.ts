import { expect } from "@playwright/test"
import {
  fillActiveCitationInputs,
  fillEnsuingDecisionInputs,
  fillPreviousDecisionInputs,
  navigateToCategories,
  publishDocumentationUnit,
  waitForSaving,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test("manually added items are saved, can be edited and deleted", async ({
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
      await container.getByLabel("Listen Eintrag").click()
      await container
        .getByLabel("Aktenzeichen " + section, { exact: true })
        .fill(fileNumber2)

      await container.getByLabel(section + " speichern").click()
      await expect(container.getByText(fileNumber1)).toBeHidden()
      await expect(container.getByText(fileNumber2)).toBeVisible()

      await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

      // add second entry
      await container.getByLabel("Weitere Angabe").click()
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

      await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
      await page.reload()
      const listEntries = container.getByLabel("Listen Eintrag")
      await expect(listEntries).toHaveCount(2)
      await listEntries.first().click()
      await container.getByLabel("Eintrag löschen").click()
      await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
    })
  }
})

/* eslint-disable playwright/no-conditional-in-test */
test("search for documentunits and link decision", async ({
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

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, inputs)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, inputs)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, inputs)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()

        await expect(container.getByText("1 Ergebnis gefunden")).toBeVisible()

        let summary = `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`

        const result = container.getByText(summary)
        await expect(result).toBeVisible()
        await container.getByLabel("Treffer übernehmen").click()

        //make sure to have type in list
        if (container === ensuingDecisionContainer) {
          summary = `nachgehend, ` + summary
        }

        const listItem = container.getByLabel("Listen Eintrag").last()
        await expect(listItem).toBeVisible()
        await expect(listItem).toContainText(summary, { useInnerText: true })

        // search for same parameters gives same result, indication that decision is already added
        await container.getByLabel("Weitere Angabe").click()

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, inputs)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, inputs)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, inputs)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()

        await expect(container.getByText("1 Ergebnis gefunden")).toBeVisible()
        await expect(container.getByText("Bereits hinzugefügt")).toBeVisible()

        //can be edited
        await container.getByLabel("Listen Eintrag").last().click()

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, { citationType: "Änderung" })
          await container.getByLabel("Aktivzitierung speichern").click()
          //make sure to have citation style in list
          summary = `Änderung, ` + summary
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, {
            deviatingFileNumber: "deviating file number",
          })
          await container
            .getByLabel("Vorgehende Entscheidung speichern")
            .click()
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, { note: "Vermerk" })
          await container
            .getByLabel("Nachgehende Entscheidung speichern")
            .click()
        }

        //can be deleted
        await container.getByLabel("Listen Eintrag").last().click()
        await container.getByLabel("Eintrag löschen").click()
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

        await expect(
          container.getByLabel("Listen Eintrag").last(),
        ).not.toHaveText(summary)
      },
    )
  }
})

test("search with changed parameters resets the page to 0", async ({
  page,
  prefilledDocumentUnit,
}) => {
  await navigateToCategories(page, prefilledDocumentUnit.documentNumber || "")
  await expect(
    page.getByText(prefilledDocumentUnit.documentNumber || ""),
  ).toBeVisible()

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
        await container.getByLabel("Nach Entscheidung suchen").click()
        await expect(container.getByText("Seite 1")).toBeVisible()

        await container.getByLabel("nächste Ergebnisse").click()
        await expect(container.getByText("Seite 2")).toBeVisible()

        const input = { fileNumber: "I do not exist" }

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, input)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, input)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, input)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()

        await expect(
          container.getByText("Keine Ergebnisse gefunden."),
        ).toBeVisible()
      },
    )
  }
})

test("search for documentunits does not return current documentation unit", async ({
  page,
  prefilledDocumentUnit,
}) => {
  await publishDocumentationUnit(
    page,
    prefilledDocumentUnit.documentNumber || "",
  )

  await navigateToCategories(page, prefilledDocumentUnit.documentNumber || "")

  const activeCitationContainer = page.getByLabel("Aktivzitierung", {
    exact: true,
  })
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
          fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
          documentType: prefilledDocumentUnit.coreData.documentType?.label,
          decisionDate: "31.12.2019",
        }

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, inputs)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, inputs)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, inputs)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()
        await expect(
          container.getByText("Keine Ergebnisse gefunden."),
        ).toBeVisible()
      },
    )
  }
})

/* eslint-enable playwright/no-conditional-in-test */
