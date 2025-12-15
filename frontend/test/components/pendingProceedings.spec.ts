import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedings from "@/components/PendingProceedings.vue"
import { Court } from "@/domain/court"
import { Decision } from "@/domain/decision"

import RelatedPendingProceeding from "@/domain/pendingProceedingReference"
import documentUnitService from "@/services/documentUnitService"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

function renderComponent(
  relatedPendingProceedings?: RelatedPendingProceeding[],
) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  return {
    user,
    ...render(PendingProceedings, {
      props: { label: "Verknüpfung anhängiges Verfahren" },
      global: {
        directives: {
          "ctrl-enter": onSearchShortcutDirective,
        },
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new Decision("123", {
                    documentNumber: "1234567891234",
                    contentRelatedIndexing: {
                      relatedPendingProceedings:
                        relatedPendingProceedings ?? [],
                    },
                  }),
                },
              },
            }),
          ],
          [router],
        ],
      },
    }),
  }
}

function generateRelatedPendingProceeding(options?: {
  uuid?: string
  localId?: string
  documentNumber?: string
  court?: Court
  decisionDate?: string
  fileNumber?: string
}) {
  return new RelatedPendingProceeding({
    uuid: options?.uuid ?? crypto.randomUUID(),
    localId: options?.localId ?? "0",
    documentNumber: options?.documentNumber ?? undefined,
    court: options?.court ?? {
      type: "type1",
      location: "location1",
      label: "label1",
    },
    decisionDate: options?.decisionDate ?? "2022-02-01",
    fileNumber: options?.fileNumber ?? "test fileNumber",
  })
}

describe("Verknüpfung anhängiges Verfahren", () => {
  beforeEach(() => {
    vi.spyOn(
      documentUnitService,
      "searchByRelatedDocumentation",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [
            new RelatedPendingProceeding({
              documentNumber: "YYTestDoc0017",
              court: {
                type: "BGH",
                label: "BGH",
              },
              decisionDate: "2022-02-01",
              fileNumber: "IV R 99/99",
            }),
            new RelatedPendingProceeding({
              documentNumber: "YYTestDoc0018",
              court: {
                type: "BFH",
                label: "BFH",
              },
              decisionDate: "2025-05-05",
              fileNumber: "AV R 77/77",
            }),
          ],
          size: 0,
          number: 0,
          numberOfElements: 20,
          first: true,
          last: false,
          empty: false,
        },
      }),
    )
    vi.spyOn(window, "scrollTo").mockImplementation(() => vi.fn())
    window.HTMLElement.prototype.scrollIntoView = vi.fn()
  })

  it("renders related pending proceedings as list entries", () => {
    renderComponent([
      generateRelatedPendingProceeding({
        documentNumber: "YYTestDoc0017",
        court: {
          type: "BGH",
          label: "BGH",
        },
        decisionDate: "2022-02-01",
        fileNumber: "IV R 99/99",
      }),
      generateRelatedPendingProceeding({
        documentNumber: "YYTestDoc0018",
        court: {
          type: "BFH",
          label: "BFH",
        },
        decisionDate: "2025-05-05",
        fileNumber: "AV R 77/77",
      }),
    ])

    expect(screen.getByText("Verknüpfung anhängiges Verfahren")).toBeVisible()
    expect(screen.getByText(/bgh, 01\.02\.2022, iv r 99\/99 \|/i)).toBeVisible()
    expect(screen.getByRole("link", { name: "YYTestDoc0017" })).toBeVisible()
    expect(screen.getByText(/bfh, 05\.05\.2025, av r 77\/77 \|/i)).toBeVisible()
    expect(screen.getByRole("link", { name: "YYTestDoc0018" })).toBeVisible()
  })

  it("renders empty related pending proceeding in edit mode, when no relatedPendingProceedings in list", async () => {
    renderComponent()
    expect(screen.getByText("Dokumentnummer")).toBeInTheDocument()
    expect(screen.getByText("Aktenzeichen")).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Nach anhängigen Verfahren suchen" }),
    ).toBeEnabled()
  })

  it("click on list item, opens the list entry in edit mode", async () => {
    const { user } = renderComponent([
      generateRelatedPendingProceeding({
        documentNumber: "YYTestDoc0017",
        court: {
          type: "BGH",
          label: "BGH",
        },
        decisionDate: "2022-02-01",
        fileNumber: "IV R 99/99",
      }),
    ])

    expect(
      screen.queryByLabelText("Verknüpfung anhängiges Verfahren speichern"),
    ).not.toBeInTheDocument()

    await user.click(screen.getByTestId("list-entry-0"))

    expect(
      screen.getByLabelText("Dokumentnummer anhängiges Verfahren"),
    ).toHaveAttribute("readonly")
    expect(
      screen.getByLabelText("Aktenzeichen anhängiges Verfahren"),
    ).toHaveAttribute("readonly")
    expect(
      screen.getByRole("button", { name: "Abbrechen" }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Eintrag löschen" }),
    ).toBeInTheDocument()
  })

  it("correctly deletes related pending proceeding", async () => {
    const { user } = renderComponent([
      generateRelatedPendingProceeding({ localId: "0" }),
      generateRelatedPendingProceeding({ localId: "1" }),
    ])
    const relatedPendingProceedings = screen.getAllByLabelText("Listen Eintrag")
    expect(relatedPendingProceedings.length).toBe(2)

    await user.click(screen.getByTestId("list-entry-0"))
    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("renders from search added related pending proceedings as editable list item", async () => {
    const { user } = renderComponent()

    const documentNumberInput = screen.getByLabelText(
      "Dokumentnummer anhängiges Verfahren",
    )
    await user.type(documentNumberInput, "YYTestDoc0017")

    const searchButton = screen.getByRole("button", {
      name: "Nach anhängigen Verfahren suchen",
    })

    await user.click(searchButton)

    const button = screen.getAllByRole("button", {
      name: "Treffer übernehmen",
    })[0]

    await user.click(button)

    expect(screen.getByText(/bgh, 01\.02\.2022, iv r 99\/99 \|/i)).toBeVisible()
  })

  it("lists search results", async () => {
    const { user } = renderComponent()

    expect(screen.queryByText(/YYTestDoc0017/)).not.toBeInTheDocument()
    await user.click(
      await screen.findByRole("button", {
        name: "Nach anhängigen Verfahren suchen",
      }),
    )

    expect(screen.getAllByText(/YYTestDoc0017/).length).toBe(1)
  })

  it("search is triggered with shortcut", async () => {
    const { user } = renderComponent()

    expect(screen.queryByText(/YYTestDoc0017/)).not.toBeInTheDocument()
    await user.type(
      await screen.findByLabelText("Aktenzeichen anhängiges Verfahren"),
      "test",
    )
    await user.keyboard("{Control>}{Enter}")

    expect(screen.getAllByText(/YYTestDoc0017/).length).toBe(1)
  })

  it("adds related pending proceeding from search results", async () => {
    const { user } = renderComponent()

    const searchButton = screen.getByRole("button", {
      name: "Nach anhängigen Verfahren suchen",
    })
    await user.click(searchButton)

    const button = screen.getAllByRole("button", {
      name: "Treffer übernehmen",
    })[0]

    await user.click(button)
    expect(screen.getAllByText(/YYTestDoc0017/).length).toBe(1)
  })

  it("indicates that search result already added to related pending proceedings", async () => {
    const relatedPendingProceedings: RelatedPendingProceeding[] = [
      generateRelatedPendingProceeding({ documentNumber: "YYTestDoc0017" }),
    ]
    const { user } = renderComponent(relatedPendingProceedings)
    await user.click(screen.getByText(/Weitere Angabe/))
    const searchButton = screen.getByRole("button", {
      name: "Nach anhängigen Verfahren suchen",
    })
    await user.click(searchButton)

    expect(screen.getByText(/Bereits hinzugefügt/)).toBeInTheDocument()
  })
})
