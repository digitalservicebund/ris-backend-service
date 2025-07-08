import { expect, Locator, Page } from "@playwright/test"
import dayjs from "dayjs"
import { fillInput, navigateToSearch } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import PendingProceeding from "@/domain/pendingProceeding"
import { deleteDocumentUnit } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import { generateString } from "~/test-helper/dataGenerators"

/* eslint-disable playwright/expect-expect */
/* eslint-disable playwright/no-nested-step */
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
      pendingProceedingsFromOptions,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } =
        pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix(fileNumberPrefix, pageWithBfhUser)
      await triggerSearch(pageWithBfhUser)
      await checkResultListContent(createdPendingProceedings, pageWithBfhUser)
    })

    test("Suche nach Gerichtstyp", async ({
      pageWithBfhUser,
      pendingProceedingsFromOptions,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } =
        pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix(fileNumberPrefix, pageWithBfhUser)
      await test.step("Wähle Gerichtstyp 'BFH' in Suche", async () => {
        await fillInput(pageWithBfhUser, "Gerichtstyp Suche", "BFH")
      })
      await triggerSearch(pageWithBfhUser)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.coreData.court?.label === "BFH",
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Gerichtsort", async ({
      pageWithBfhUser,
      pendingProceedingsFromOptions,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } =
        pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix(fileNumberPrefix, pageWithBfhUser)
      await test.step("Wähle Gerichtsort 'Aachen' in Suche", async () => {
        await fillInput(pageWithBfhUser, "Gerichtsort Suche", "Aachen")
      })
      await triggerSearch(pageWithBfhUser)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.coreData.court?.label === "AG Aachen",
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Mitteilungsdatum", async ({
      pageWithBfhUser,
      pendingProceedingsFromOptions,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } =
        pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix(fileNumberPrefix, pageWithBfhUser)
      await test.step("Wähle Mitteilungsdatum '02.01.2023' in Suche", async () => {
        await fillInput(pageWithBfhUser, "Mitteilungsdatum Suche", "02.01.2023")
      })
      await triggerSearch(pageWithBfhUser)
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
      await triggerSearch(pageWithBfhUser)
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
      pendingProceedingsFromOptions,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } =
        pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix(fileNumberPrefix, pageWithBfhUser)
      await test.step("Wähle Erledigungsmitteilung '01.01.2024' in Suche", async () => {
        await fillInput(
          pageWithBfhUser,
          "Erledigungsmitteilung Suche",
          "01.01.2024",
        )
      })
      await triggerSearch(pageWithBfhUser)
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
      await triggerSearch(pageWithBfhUser)
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
      pendingProceedingsFromOptions,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } =
        pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix(fileNumberPrefix, pageWithBfhUser)
      await test.step("Wähle Erledigt Checkbox in Suche aus", async () => {
        await pageWithBfhUser
          .getByRole("checkbox", { name: "Erledigt Filter" })
          .check()
      })
      await triggerSearch(pageWithBfhUser)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.coreData.isResolved,
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Dokumentnummer", async ({
      pageWithBfhUser,
      pendingProceedingsFromOptions,
    }) => {
      const { fileNumberPrefix, createdPendingProceedings } =
        pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix(fileNumberPrefix, pageWithBfhUser)
      const docNumber = createdPendingProceedings[4].documentNumber
      await test.step(`Wähle Dokumentnummer '${docNumber}' in Suche`, async () => {
        await fillInput(pageWithBfhUser, "Dokumentnummer Suche", docNumber)
      })
      await triggerSearch(pageWithBfhUser)
      const docUnitSearchResults = createdPendingProceedings.filter(
        (p) => p.documentNumber === docNumber,
      )
      await checkResultListContent(docUnitSearchResults, pageWithBfhUser)
    })

    test("Suche nach Status", async ({
      pageWithBfhUser,
      pendingProceedingsFromOptions,
    }) => {
      const { createdPendingProceedings } = pendingProceedingsFromOptions
      await openSearchWithFileNumberPrefix("I R 20000/34", pageWithBfhUser)
      await test.step(`Wähle Status 'Veröffentlicht' in Suche`, async () => {
        await pageWithBfhUser.getByLabel("Status Suche").click()
        await pageWithBfhUser
          .getByRole("option", { name: "Veröffentlicht", exact: true })
          .click()
      })
      await triggerSearch(pageWithBfhUser)
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
      await openSearchWithFileNumberPrefix("", pageWithBfhUser)
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
        await openSearchWithFileNumberPrefix("", pageWithBfhUser)
        await fillInput(
          pageWithBfhUser,
          "Dokumentnummer Suche",
          documentNumberToBeDeleted,
        )
        await triggerSearch(pageWithBfhUser)
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
        await pageWithBfhUser
          .getByRole("button", { name: "Dokumentationseinheit löschen" })
          .click()
        await pageWithBfhUser
          .getByRole("button", { name: "Löschen", exact: true })
          .click()
        await expect(
          pageWithBfhUser.getByText("Keine Suchergebnisse gefunden"),
        ).toBeVisible()
      })
      await test.step("Suche nach neuer Dokumentnummer ergibt kein Ergebnis", async () => {
        await openSearchWithFileNumberPrefix("", pageWithBfhUser)
        await fillInput(
          pageWithBfhUser,
          "Dokumentnummer Suche",
          documentNumberToBeDeleted,
        )
        await triggerSearch(pageWithBfhUser)
        await expect(
          pageWithBfhUser.getByText("Keine Suchergebnisse gefunden"),
        ).toBeVisible()
        documentNumberToBeDeleted = undefined
      })
    })

    test("Neuanlage aus Suche", async ({ pageWithBfhUser }) => {
      const fileNumber = generateString()
      await openSearchWithFileNumberPrefix("", pageWithBfhUser)
      await test.step("Suche nach Gericht, Datum und Aktenzeichen", async () => {
        await fillInput(pageWithBfhUser, "Aktenzeichen Suche", fileNumber)
        await fillInput(pageWithBfhUser, "Gerichtstyp Suche", "BFH")
        await fillInput(pageWithBfhUser, "Mitteilungsdatum Suche", "05.07.2022")
        await triggerSearch(pageWithBfhUser)
      })
      await test.step("Ohne Ergebnisse kann Neuanlage aus Suchparametern erfolgen", async () => {
        await expect(
          pageWithBfhUser.getByText("folgenden Stammdaten übernehmen"),
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

async function openSearchWithFileNumberPrefix(
  commonFileNumberPrefix: string,
  pageWithBfhUser: Page,
) {
  await navigateToSearch(pageWithBfhUser)
  await test.step("Wähle Tab 'Anhängige Verfahren' aus", async () => {
    await pageWithBfhUser.getByTestId("search-tab-pending-proceeding").click()
    await expect(pageWithBfhUser).toHaveURL(/pending-proceedings/)
  })
  if (commonFileNumberPrefix) {
    await test.step("Befülle Suche mit Aktenzeichen-Prefix", async () => {
      await fillInput(
        pageWithBfhUser,
        "Aktenzeichen Suche",
        commonFileNumberPrefix,
      )
    })
  }
}

async function triggerSearch(pageWithBfhUser: Page) {
  await test.step("Führe Suche aus", async () => {
    await pageWithBfhUser
      .getByRole("button", { name: "Nach Anhängigen Verfahren" })
      .click()
  })
}

/**
 * Check for given pending proceedings that the result list contains the expected content cell by cell.
 */
async function checkResultListContent(
  pendingProceedings: PendingProceeding[],
  pageWithBfhUser: Page,
) {
  const expectedResultsCountText =
    pendingProceedings.length > 1
      ? `${pendingProceedings.length} Ergebnisse gefunden`
      : "1 Ergebnis gefunden"
  await test.step(`Prüfe, dass ${expectedResultsCountText}`, async () => {
    await expect(
      pageWithBfhUser.getByText(expectedResultsCountText),
    ).toBeVisible()
  })

  await test.step("Prüfe Inhalte in sortierter Ergebnisliste", async () => {
    // Index is not zero-based because of table header row.
    for (let i = 1; i <= pendingProceedings.length; i++) {
      // Reversed list as we sort by date DESC and docNumber DESC (most recent first).
      const listItem = pendingProceedings[pendingProceedings.length - i]
      const listRow = pageWithBfhUser.getByRole("row").nth(i)
      await test.step(`Prüfe ${i}. Ergebnis in Liste`, async () => {
        await checkContentOfResultRow(listRow, listItem)
      })
    }
  })
}

async function checkContentOfResultRow(
  listRow: Locator,
  expectedItem: PendingProceeding,
) {
  const docNumberCell = listRow.getByRole("cell").nth(0)
  const courtTypeCell = listRow.getByRole("cell").nth(1)
  const decisionDateCell = listRow.getByRole("cell").nth(2)
  const fileNumberCell = listRow.getByRole("cell").nth(3)
  const statusCell = listRow.getByRole("cell").nth(4)
  const errorCell = listRow.getByRole("cell").nth(5)
  const resolutionDateCell = listRow.getByRole("cell").nth(6)

  await test.step("Dokumentnummer", async () => {
    await expect(docNumberCell).toHaveText(expectedItem.documentNumber)
  })
  await test.step("Gerichtstyp", async () => {
    await expect(courtTypeCell).toHaveText(
      expectedItem.coreData.court?.type ?? "-",
    )
  })
  await test.step("Mitteilungsdatum", async () => {
    // eslint-disable-next-line playwright/no-conditional-in-test
    const formattedDate = expectedItem.coreData.decisionDate
      ? dayjs(expectedItem.coreData.decisionDate).format("DD.MM.YYYY")
      : "-"
    await expect(decisionDateCell).toHaveText(formattedDate)
  })
  await test.step("Aktenzeichen", async () => {
    await expect(fileNumberCell).toHaveText(
      expectedItem.coreData.fileNumbers?.[0] ?? "-",
    )
  })
  await test.step("Veröffentlichungsstatus", async () => {
    await expect(statusCell).toHaveText("Unveröffentlicht")
  })
  await test.step("Fehler", async () => {
    await expect(errorCell).toHaveText("-")
  })
  await test.step("Erledigungsmitteilung", async () => {
    // eslint-disable-next-line playwright/no-conditional-in-test
    const formattedResolutionDate = expectedItem.coreData.resolutionDate
      ? dayjs(expectedItem.coreData.resolutionDate).format("DD.MM.YYYY")
      : "-"
    await expect(resolutionDateCell).toHaveText(formattedResolutionDate)
  })
  await test.step("Kann bearbeitet, angesehen und gelöscht werden", async () => {
    await expect(
      listRow.getByLabel("Dokumentationseinheit bearbeiten"),
    ).toBeEnabled()
    await expect(
      listRow.getByLabel("Dokumentationseinheit ansehen"),
    ).toBeEnabled()
    await expect(
      listRow.getByLabel("Dokumentationseinheit löschen"),
    ).toBeEnabled()
  })
}
