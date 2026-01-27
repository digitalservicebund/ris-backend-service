import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { beforeEach, describe } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import CategoryImport from "@/components/category-import/CategoryImport.vue"
import { AppealWithdrawal, PkhPlaintiff } from "@/domain/appeal"
import { AppealAdmitter } from "@/domain/appealAdmitter"
import Correction from "@/domain/correction"
import {
  allLabels,
  contentRelatedIndexingLabels,
  Decision,
} from "@/domain/decision"
import Definition from "@/domain/definition"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { Kind } from "@/domain/documentationUnitKind"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import NormReference from "@/domain/normReference"
import PendingProceeding from "@/domain/pendingProceeding"
import RelatedPendingProceeding from "@/domain/pendingProceedingReference"
import { PublicationState } from "@/domain/publicationStatus"
import SingleNorm from "@/domain/singleNorm"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

describe("CategoryImport", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.resetAllMocks()
  })
  it("renders component initial state", () => {
    renderComponent()

    expect(screen.getByText("Rubriken importieren")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeInTheDocument()
  })

  it("enables button when typing document number with valid length", async () => {
    const { user } = renderComponent()

    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeDisabled()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "XXRE123456789",
    )

    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeEnabled()
  })

  it("displays error when no document unit found", async () => {
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
      () => Promise.resolve({ status: 400, error: { title: "error" } }),
    )

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "XXRE123456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    expect(
      screen.getByText("Keine Dokumentationseinheit gefunden."),
    ).toBeInTheDocument()
  })

  it("should import a string (E-VSF) from content related indexing", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      contentRelatedIndexing: { evsf: "X 00 00-0-0" },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(screen.getByLabelText("E-VSF übernehmen"))

    expect(store.documentUnit?.contentRelatedIndexing.evsf).toEqual(
      "X 00 00-0-0",
    )
  })

  it("should import an editable list (definitions) from content related indexing", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      contentRelatedIndexing: {
        definitions: [
          new Definition({ id: "def-id", definedTerm: "term" }),
          new Definition({ id: "def-id2", definedTerm: "term2" }),
        ],
      },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(screen.getByLabelText("Definition übernehmen"))

    const definitions = store.documentUnit?.contentRelatedIndexing.definitions
    expect(definitions).toHaveLength(2)
    expect(definitions?.[0].definedTerm).toEqual("term")
    expect(definitions?.[0].id).toBeUndefined()
    expect(definitions?.[1].definedTerm).toEqual("term2")
    expect(definitions?.[1].id).toBeUndefined()
  })

  it("should import an object (Rechtsmittelzulassung) from content related indexing", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      contentRelatedIndexing: {
        appealAdmission: { admitted: true, by: AppealAdmitter.FG },
      },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(
      screen.getByLabelText("Rechtsmittelzulassung übernehmen"),
    )

    expect(store.documentUnit?.contentRelatedIndexing.appealAdmission).toEqual({
      admitted: true,
      by: AppealAdmitter.FG,
    })
  })

  it("should import an object (Rechtsmittel) from content related indexing", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      contentRelatedIndexing: {
        appeal: {
          id: "id123",
          appellants: [{ id: "1", value: "Kläger" }],
          revisionDefendantStatuses: [],
          revisionPlaintiffStatuses: [],
          jointRevisionDefendantStatuses: [{ id: "2", value: "unbegründet" }],
          jointRevisionPlaintiffStatuses: [],
          nzbDefendantStatuses: [],
          nzbPlaintiffStatuses: [],
          appealWithdrawal: AppealWithdrawal.NEIN,
          pkhPlaintiff: PkhPlaintiff.KEINE_ANGABE,
        },
      },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(screen.getByLabelText("Rechtsmittel übernehmen"))

    expect(store.documentUnit?.contentRelatedIndexing.appeal).toEqual({
      id: "uuid",
      appellants: [{ id: "1", value: "Kläger" }],
      revisionDefendantStatuses: [],
      revisionPlaintiffStatuses: [],
      jointRevisionDefendantStatuses: [{ id: "2", value: "unbegründet" }],
      jointRevisionPlaintiffStatuses: [],
      nzbDefendantStatuses: [],
      nzbPlaintiffStatuses: [],
      appealWithdrawal: AppealWithdrawal.NEIN,
      pkhPlaintiff: PkhPlaintiff.KEINE_ANGABE,
    })
  })

  it("should import a list (decision names) from short texts", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      shortTexts: { decisionNames: ["foo", "bar"] },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(
      screen.getByLabelText("Entscheidungsnamen übernehmen"),
    )

    expect((store.documentUnit as Decision)?.shortTexts.decisionNames).toEqual([
      "foo",
      "bar",
    ])
  })

  it("should import a string (headline) from short texts", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      shortTexts: { headline: "headline" },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(screen.getByLabelText("Titelzeile übernehmen"))

    expect(store.documentUnit?.shortTexts.headline).toEqual("headline")
  })

  it("should import a list (decision names) from short texts", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      shortTexts: { decisionNames: ["foo", "bar"] },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(
      screen.getByLabelText("Entscheidungsnamen übernehmen"),
    )

    expect((store.documentUnit as Decision)?.shortTexts.decisionNames).toEqual([
      "foo",
      "bar",
    ])
  })

  it("should import corrections", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      longTexts: {
        corrections: [
          new Correction({
            id: "a4ba90c3-bb99-4c67-b5dd-b0b37a80c9de",
            type: "Unrichtigkeiten",
          }),
        ],
      },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(screen.getByLabelText("Berichtigung übernehmen"))

    expect((store.documentUnit as Decision).longTexts.corrections).toHaveLength(
      1,
    )
    expect(
      (store.documentUnit as Decision).longTexts.corrections?.[0].type,
    ).toEqual("Unrichtigkeiten")
  })

  it("should import related pending proceedings (Verknüpfung anhängiges Verfahren)", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      contentRelatedIndexing: {
        relatedPendingProceedings: [
          {
            documentNumber: "YYTestDoc0017",
            court: {
              type: "BGH",
              label: "BGH",
            },
            decisionDate: "2022-02-01",
            fileNumber: "IV R 99/99",
          } as RelatedPendingProceeding,
        ],
      },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(
      screen.getByLabelText("Verknüpfung anhängiges Verfahren übernehmen"),
    )

    expect(
      (store.documentUnit as Decision).contentRelatedIndexing
        .relatedPendingProceedings,
    ).toHaveLength(1)
    expect(
      (store.documentUnit as Decision).contentRelatedIndexing
        ?.relatedPendingProceedings?.[0].documentNumber,
    ).toEqual("YYTestDoc0017")
  })

  it("should import non-application norms (Nichtanwendungsgesetz)", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
      contentRelatedIndexing: {
        nonApplicationNorms: [],
      },
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      contentRelatedIndexing: {
        nonApplicationNorms: [
          new NormReference({
            normAbbreviation: { abbreviation: "ABC" },
            singleNorms: [
              new SingleNorm({
                singleNorm: "§ 1",
              }),
              new SingleNorm({
                singleNorm: "§ 2",
              }),
            ],
          }),
        ],
      },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(
      screen.getByLabelText("Nichtanwendungsgesetz übernehmen"),
    )

    expect(
      (store.documentUnit as Decision).contentRelatedIndexing
        .nonApplicationNorms,
    ).toHaveLength(1)
    expect(
      (store.documentUnit as Decision).contentRelatedIndexing
        ?.nonApplicationNorms?.[0].normAbbreviation?.abbreviation,
    ).toEqual("ABC")
    expect(
      (store.documentUnit as Decision).contentRelatedIndexing
        ?.nonApplicationNorms?.[0].singleNorms?.[0].singleNorm,
    ).toEqual("§ 1")
  })

  it("should import norms (Normen)", async () => {
    const target = new Decision("uuid", {
      documentNumber: "XXRE123456789",
      kind: Kind.DECISION,
      contentRelatedIndexing: {
        norms: [],
      },
    })
    const source = new Decision("456", {
      kind: Kind.DECISION,
      documentNumber: "TARGET3456789",
      contentRelatedIndexing: {
        norms: [
          new NormReference({
            normAbbreviation: { abbreviation: "ABC" },
            singleNorms: [
              new SingleNorm({
                singleNorm: "§ 1",
              }),
              new SingleNorm({
                singleNorm: "§ 2",
              }),
            ],
          }),
        ],
      },
    })
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockResolvedValueOnce({
      status: 200,
      data: source,
    })
    const store = mockSessionStore(target)

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "TARGET3456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    await fireEvent.click(screen.getByLabelText("Normen übernehmen"))

    expect(
      (store.documentUnit as Decision).contentRelatedIndexing.norms,
    ).toHaveLength(1)
    expect(
      (store.documentUnit as Decision).contentRelatedIndexing?.norms?.[0]
        .normAbbreviation?.abbreviation,
    ).toEqual("ABC")
    expect(
      (store.documentUnit as Decision).contentRelatedIndexing?.norms?.[0]
        .singleNorms?.[0].singleNorm,
    ).toEqual("§ 1")
  })

  it("displays core data when document unit found", async () => {
    vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
      () =>
        Promise.resolve({
          status: 200,
          data: new Decision("foo", {
            documentNumber: "XXRE123456789",
            status: {
              publicationStatus: PublicationState.UNPUBLISHED,
            },
            coreData: {
              court: {
                label: "AG Aachen",
              },
              fileNumbers: ["file-123"],
              decisionDate: "2022-02-01",
            },
          }),
        }),
    )

    const { user } = renderComponent()

    await user.type(
      screen.getByLabelText("Dokumentnummer Eingabefeld"),
      "XXRE123456789",
    )

    await fireEvent.click(
      screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
    )

    expect(screen.getByText("XXRE123456789")).toBeInTheDocument()
    expect(screen.getByText(/AG Aachen, 01.02.2022, file-123,/)).toBeVisible()
    expect(screen.getByText("Unveröffentlicht")).toBeInTheDocument()
  })

  describe("Pending proceeding", () => {
    it("displays exclusively contentRelatedIndexing categories when the source is a pending proceeding", async () => {
      const source = new PendingProceeding("q834")
      const target = new Decision("uuid", {
        documentNumber: "XXRE123456789",
        kind: Kind.DECISION,
      })
      vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
        () => Promise.resolve({ status: 200, data: source }),
      )
      mockSessionStore(target)

      const { user } = renderComponent()

      await user.type(
        screen.getByLabelText("Dokumentnummer Eingabefeld"),
        "XXRE123456789",
      )

      await fireEvent.click(
        screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
      )

      for (const label of ["Schlagwörter", "Sachgebiete", "Normen"]) {
        expect(screen.getByText(label)).toBeInTheDocument()
      }
      const allOtherLabels = Object.values(allLabels).filter(
        (label) => !["Schlagwörter", "Sachgebiete", "Normen"].includes(label),
      )
      for (const label of allOtherLabels) {
        expect(screen.queryByText(label)).not.toBeInTheDocument()
      }
    })

    it("displays exclusively contentRelatedIndexing categories when the target is a pending proceeding", async () => {
      const source = new Decision("q834")
      const target = new PendingProceeding("uuid", {
        documentNumber: "XXRE123456789",
        kind: Kind.PENDING_PROCEEDING,
      })
      vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
        () => Promise.resolve({ status: 200, data: source }),
      )
      mockSessionStore(target)

      const { user } = renderComponent()

      await user.type(
        screen.getByLabelText("Dokumentnummer Eingabefeld"),
        "XXRE123456789",
      )

      await fireEvent.click(
        screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
      )

      for (const label of ["Schlagwörter", "Sachgebiete", "Normen"]) {
        expect(screen.getByText(label)).toBeInTheDocument()
      }
      const allOtherLabels = Object.values(allLabels).filter(
        (label) => !["Schlagwörter", "Sachgebiete", "Normen"].includes(label),
      )
      for (const label of allOtherLabels) {
        expect(screen.queryByText(label)).not.toBeInTheDocument()
      }
    })

    it("when user clicks 'übernehmen'-buttons, 'Übernommen' is displayed", async () => {
      const source = new PendingProceeding("q834", {
        contentRelatedIndexing: {
          keywords: ["Schlagwort 1", "Schlagwort 2"],
          fieldsOfLaw: [
            generateFieldOfLaw("AB-01", "Text for AB links to CD-01"),
          ],
          norms: [
            new NormReference({
              normAbbreviationRawValue: "ABC",
            }),
          ],
        },
      })
      const target = new Decision("uuid", {
        documentNumber: "XXRE123456789",
        kind: Kind.DECISION,
      })
      vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
        () => Promise.resolve({ status: 200, data: source }),
      )
      mockSessionStore(target)

      const { user } = renderComponent()

      await user.type(
        screen.getByLabelText("Dokumentnummer Eingabefeld"),
        "XXRE123456789",
      )

      await fireEvent.click(
        screen.getByRole("button", { name: "Dokumentationseinheit laden" }),
      )

      await fireEvent.click(
        screen.getByLabelText(
          contentRelatedIndexingLabels.keywords + " übernehmen",
        ),
      )
      await fireEvent.click(
        screen.getByLabelText(
          contentRelatedIndexingLabels.fieldsOfLaw + " übernehmen",
        ),
      )
      await fireEvent.click(
        screen.getByLabelText(
          contentRelatedIndexingLabels.norms + " übernehmen",
        ),
      )
      expect(screen.getAllByText("Übernommen").length).toBe(3)
    })
  })

  function generateFieldOfLaw(identifier: string, text: string): FieldOfLaw {
    return {
      identifier: identifier,
      text: text,
      linkedFields: ["CD-01"],
      norms: [],
      children: [],
      hasChildren: false,
    }
  }
})

function mockSessionStore(documentUnit: DocumentationUnit) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = documentUnit
  vi.spyOn(mockedSessionStore, "updateDocumentUnit").mockResolvedValue({
    status: 200,
    data: { documentationUnitVersion: 0, patch: [], errorPaths: [] },
  })
  return mockedSessionStore
}

function renderComponent() {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(CategoryImport, { global: { plugins: [[router]] } }),
  }
}
