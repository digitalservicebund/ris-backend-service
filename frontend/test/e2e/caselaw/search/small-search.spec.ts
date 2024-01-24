import { expect } from "@playwright/test"
import {
  fillActiveCitationInputs,
  fillEnsuingDecisionInputs,
  fillPreviousDecisionInputs,
  navigateToCategories,
  navigateToPublication,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

/* eslint-disable playwright/no-conditional-in-test */
test("search for documentunits and link decision", async ({
  page,
  documentNumber,
  prefilledDocumentUnit,
}) => {
  await navigateToPublication(page, prefilledDocumentUnit.documentNumber || "")

  await page
    .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
    .click()
  await expect(page.locator("text=Email wurde versendet")).toBeVisible()

  await expect(page.locator("text=Xml Email Abgabe -")).toBeVisible()
  await expect(page.locator("text=In Veröffentlichung")).toBeVisible()

  await navigateToCategories(page, documentNumber)
  await expect(page.getByText(documentNumber)).toBeVisible()

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

        await expect(container.getByText("Seite 1")).toBeVisible()

        let summary = `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil, ${prefilledDocumentUnit.documentNumber}`

        const result = container.getByText(summary)
        await expect(result).toBeVisible()
        await container.getByLabel("Treffer übernehmen").click()

        //make sure to have type in list
        if (container === ensuingDecisionContainer) {
          summary = `nachgehend, ` + summary
        }

        const listItem = container.getByLabel("Listen Eintrag").last()
        await expect(listItem).toBeVisible()
        await expect(listItem).toHaveText(summary, { useInnerText: true })

        await expect(container.getByLabel("Eintrag löschen")).toBeVisible()

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

        await expect(container.getByText("Seite 1")).toBeVisible()
        await expect(container.getByText("Bereits hinzugefügt")).toBeVisible()

        //can be edited
        await expect(container.getByLabel("Eintrag bearbeiten")).toBeVisible()
        await container.getByLabel("Eintrag bearbeiten").click()

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

        await expect(container.getByText(summary)).toBeVisible()

        //can be deleted
        await container.getByLabel("Eintrag löschen").first().click()
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
      "for category " +
        (await activeCitationContainer.first().getAttribute("aria-label")),
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

/* eslint-enable playwright/no-conditional-in-test */
