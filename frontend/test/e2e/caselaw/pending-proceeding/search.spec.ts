import { expect } from "@playwright/test"
import { Kind } from "@/domain/documentationUnitKind"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { deleteDocumentUnit } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import {
  fillInput,
  navigateToSearch,
  checkResultListContent,
  openSearchWithFileNumberPrefix,
  triggerSearch,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

/* eslint-disable playwright/expect-expect */

test.describe(
  "Große Suche nach Anhängigen Verfahren",
  {
    tag: "@RISDEV-6689",
  },
  () => {
    let documentNumberToBeDeleted: string | undefined
    test.use({
      pendingProceedingsToBeCreated: [
        // Reverse sorting: date DESC, docNumber DESC
        [
          { coreData: { court: { label: "BFH" } } },
          { coreData: { court: { label: "AG Aachen" } } },
          { coreData: { isResolved: true, resolutionDate: "2024-12-31" } },
          { coreData: { isResolved: true, resolutionDate: "2024-12-30" } },
          { coreData: { isResolved: true, resolutionDate: "2024-01-01" } },
          { coreData: { isResolved: true } },
          { coreData: { decisionDate: "2023-01-01" } },
          { coreData: { decisionDate: "2023-01-02" } },
          { coreData: { decisionDate: "2023-01-31" } },
        ],
        { scope: "test" },
      ],
    })

    test("Suche nach Aktenzeichen", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        fileNumberPrefix,
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      await checkResultListContent(createdPendingProceedings, pageWithBfhUser)
    })

    test("Suche nach Gerichtstyp", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        fileNumberPrefix,
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step("Wähle Gerichtstyp 'BFH' in Suche", async () => {
        await fillInput(pageWithBfhUser, "Gerichtstyp Suche", "BFH")
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.coreData.court?.label === "BFH",
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Gerichtsort", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        fileNumberPrefix,
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step("Wähle Gerichtsort 'Aachen' in Suche", async () => {
        await fillInput(pageWithBfhUser, "Gerichtsort Suche", "Aachen")
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.coreData.court?.label === "AG Aachen",
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Mitteilungsdatum", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        fileNumberPrefix,
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step("Wähle Mitteilungsdatum '02.01.2023' in Suche", async () => {
        await fillInput(pageWithBfhUser, "Mitteilungsdatum Suche", "02.01.2023")
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResultsSpecificDate = createdPendingProceedings.filter(
        (p) => p.coreData.decisionDate === "2023-01-02",
      )
      await checkResultListContent(
        docUnitSearchResultsSpecificDate,
        pageWithBfhUser,
      )

      await test.step("Wähle Mitteilungsdatum Ende '31.01.2023' in Suche", async () => {
        await fillInput(
          pageWithBfhUser,
          "Mitteilungsdatum Suche Ende",
          "31.01.2023",
        )
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResultsDateRange = createdPendingProceedings.filter(
        (p) =>
          p.coreData.decisionDate && p.coreData.decisionDate !== "2023-01-01",
      )
      await checkResultListContent(
        docUnitSearchResultsDateRange,
        pageWithBfhUser,
      )
    })

    test("Suche nach Erledigungsdatum", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        fileNumberPrefix,
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step("Wähle Erledigungsmitteilung '01.01.2024' in Suche", async () => {
        await fillInput(
          pageWithBfhUser,
          "Erledigungsmitteilung Suche",
          "01.01.2024",
        )
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResultsSpecificDate = createdPendingProceedings.filter(
        (p) => p.coreData.resolutionDate === "2024-01-01",
      )
      await checkResultListContent(
        docUnitSearchResultsSpecificDate,
        pageWithBfhUser,
      )

      await test.step("Wähle Erledigungsmitteilung Ende '30.12.2024' in Suche", async () => {
        await fillInput(
          pageWithBfhUser,
          "Erledigungsmitteilung Suche Ende",
          "30.12.2024",
        )
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResultsDateRange = createdPendingProceedings.filter(
        (p) =>
          p.coreData.resolutionDate &&
          p.coreData.resolutionDate !== "2024-12-31",
      )
      await checkResultListContent(
        docUnitSearchResultsDateRange,
        pageWithBfhUser,
      )
    })

    test("Suche nach erledigten Anhängigen Verfahren (Checkbox)", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        fileNumberPrefix,
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step("Wähle Erledigt Checkbox in Suche aus", async () => {
        await pageWithBfhUser
          .getByRole("checkbox", { name: "Erledigt Filter" })
          .check()
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.coreData.isResolved,
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Dokumentnummer", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        fileNumberPrefix,
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      const docNumber = createdPendingProceedings[4].documentNumber
      await test.step(`Wähle Dokumentnummer '${docNumber}' in Suche`, async () => {
        await fillInput(pageWithBfhUser, "Dokumentnummer Suche", docNumber)
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.documentNumber === docNumber,
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Status", async ({
      pageWithBfhUser,
      pendingProceedings,
    }) => {
      const { createdPendingProceedings } = pendingProceedings
      await openSearchWithFileNumberPrefix(
        "I R 20000/34",
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step(`Wähle Status 'Veröffentlicht' in Suche`, async () => {
        await pageWithBfhUser.getByLabel("Status Suche").click()
        await pageWithBfhUser
          .getByRole("option", { name: "Veröffentlicht", exact: true })
          .click()
      })
      await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      await test.step(`Prüfe, dass 1 Ergebnis gefunden wurde`, async () => {
        await expect(
          pageWithBfhUser.getByText("1 Ergebnis gefunden"),
        ).toBeVisible()
        await expect(pageWithBfhUser.getByText("YYTestDoc0017")).toBeVisible()
        await expect(
          pageWithBfhUser.getByText(
            createdPendingProceedings[0].documentNumber,
          ),
        ).toBeHidden()
      })
    })

    test("Anhängiges Verfahren neu erstellen und löschen", async ({
      pageWithBfhUser,
    }) => {
      await openSearchWithFileNumberPrefix(
        "",
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step("Klicke auf 'Neues anhängiges Verfahren'", async () => {
        await pageWithBfhUser
          .getByRole("button", { name: "Neues Anhängiges Verfahren" })
          .first()
          .click()
      })
      await test.step("Öffnet Rubriken von neuem Anhängigem Verfahren", async () => {
        await expect(pageWithBfhUser).toHaveURL(
          /\/caselaw\/pending-proceeding\/[A-Z0-9]{13}\/categories$/,
        )
        documentNumberToBeDeleted =
          /caselaw\/pending-proceeding\/(.*)\/categories/g.exec(
            pageWithBfhUser.url(),
          )?.[1] as string
        await expect(
          pageWithBfhUser.getByRole("heading", {
            name: documentNumberToBeDeleted,
          }),
        ).toBeVisible()
      })
      await test.step("Suche nach neuer Dokumentnummer", async () => {
        await openSearchWithFileNumberPrefix(
          "",
          pageWithBfhUser,
          Kind.PENDING_PROCEEDING,
        )
        await fillInput(
          pageWithBfhUser,
          "Dokumentnummer Suche",
          documentNumberToBeDeleted,
        )
        await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      })
      await test.step(`Prüfe, dass 1 Ergebnis gefunden wurde`, async () => {
        await expect(
          pageWithBfhUser.getByText("1 Ergebnis gefunden"),
        ).toBeVisible()
        await expect(
          pageWithBfhUser.getByText(documentNumberToBeDeleted!),
        ).toBeVisible()
      })
      await test.step("Lösche Anhängiges Verfahren", async () => {
        await deleteDocumentUnit(pageWithBfhUser, documentNumberToBeDeleted!)
      })
      await test.step("Suche nach neuer Dokumentnummer ergibt kein Ergebnis", async () => {
        await openSearchWithFileNumberPrefix(
          "",
          pageWithBfhUser,
          Kind.PENDING_PROCEEDING,
        )
        await fillInput(
          pageWithBfhUser,
          "Dokumentnummer Suche",
          documentNumberToBeDeleted,
        )
        await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
        await expect(
          pageWithBfhUser.getByText("Keine Suchergebnisse gefunden"),
        ).toBeVisible()
        documentNumberToBeDeleted = undefined
      })
    })

    test("Neuanlage aus Suche", async ({ pageWithBfhUser }) => {
      const fileNumber = generateString()
      await openSearchWithFileNumberPrefix(
        "",
        pageWithBfhUser,
        Kind.PENDING_PROCEEDING,
      )
      await test.step("Suche nach Gericht, Datum und Aktenzeichen", async () => {
        await fillInput(pageWithBfhUser, "Aktenzeichen Suche", fileNumber)
        await fillInput(pageWithBfhUser, "Gerichtstyp Suche", "BFH")
        await fillInput(pageWithBfhUser, "Mitteilungsdatum Suche", "05.07.2022")
        await triggerSearch(pageWithBfhUser, Kind.PENDING_PROCEEDING)
      })
      await test.step("Ohne Ergebnisse kann Neuanlage aus Suchparametern erfolgen", async () => {
        await expect(
          pageWithBfhUser.getByText("folgenden Formaldaten übernehmen"),
        ).toBeVisible()
        await expect(
          pageWithBfhUser.getByText(`${fileNumber}, BFH, 05.07.2022`),
        ).toBeVisible()
      })
      await test.step("Klicke 'Übernehmen und fortfahren'", async () => {
        await pageWithBfhUser
          .getByRole("button", { name: "Übernehmen und fortfahren" })
          .click()
      })
      await test.step("Öffnet Rubriken von neuem Anhängigem Verfahren", async () => {
        await expect(pageWithBfhUser).toHaveURL(
          /\/caselaw\/pending-proceeding\/[A-Z0-9]{13}\/categories$/,
        )
        documentNumberToBeDeleted =
          /caselaw\/pending-proceeding\/(.*)\/categories/g.exec(
            pageWithBfhUser.url(),
          )?.[1] as string
        await expect(
          pageWithBfhUser.getByRole("heading", {
            name: documentNumberToBeDeleted,
          }),
        ).toBeVisible()
      })
      await test.step("Rubriken sind bereits mit Suchparametern befüllt", async () => {
        await expect(
          pageWithBfhUser.getByLabel("Gericht", { exact: true }),
        ).toHaveValue("BFH")
        await expect(
          pageWithBfhUser.getByRole("textbox", {
            name: "Entscheidungsdatum",
            exact: true,
          }),
        ).toHaveValue("05.07.2022")
        await expect(
          pageWithBfhUser
            .getByTestId("chips-input-wrapper_fileNumber")
            .getByTestId("chip-value"),
        ).toHaveText(fileNumber)
      })
    })

    test("BGH kann Tab Anhängige Verfahren nicht aufrufen", async ({
      pageWithBghUser,
    }) => {
      await navigateToSearch(pageWithBghUser)
      await test.step("Es werden keine Tabs angezeigt", async () => {
        await expect(
          pageWithBghUser.getByTestId("search-tab-pending-proceeding"),
        ).toBeHidden()
        await expect(pageWithBghUser.getByRole("tablist")).toBeHidden()
      })
    })

    test.afterEach(async ({ pageWithBfhUser }) => {
      if (documentNumberToBeDeleted) {
        await deleteDocumentUnit(pageWithBfhUser, documentNumberToBeDeleted)
      }
    })
  },
)
