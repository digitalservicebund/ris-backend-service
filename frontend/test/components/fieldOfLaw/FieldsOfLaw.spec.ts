import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen, waitFor } from "@testing-library/vue"
import FieldsOfLawVue from "@/components/field-of-law/FieldsOfLaw.vue"
import DocumentUnit from "@/domain/documentUnit"
import FieldOfLawService from "@/services/fieldOfLawService"

function renderComponent() {
  const user = userEvent.setup()
  return {
    user,
    ...render(FieldsOfLawVue, {
      global: {
        stubs: {
          // this way the comboboxItemService is not triggered
          FieldOfLawDirectInputSearch: true,
        },
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new DocumentUnit("foo", {
                    documentNumber: "1234567891234",
                    contentRelatedIndexing: {
                      fieldsOfLaw: [],
                    },
                  }),
                },
              },
            }),
          ],
        ],
      },
    }),
  }
}

describe("FieldsOfLaw", () => {
  const getChildrenOfRoot = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: true,
          identifier: "PR",
          text: "Phantasierecht",
          linkedFields: [],
          norms: [],
          children: [],
        },
        {
          hasChildren: true,
          identifier: "AV",
          text: "Allgemeines Verwaltungsrecht",
          linkedFields: [],
          norms: [],
          children: [],
        },
        {
          identifier: "AB-01",
          text: "Text for AB",
          children: [],
          norms: [],
          isExpanded: false,
          hasChildren: false,
        },
        {
          identifier: "CD-02",
          text: "And text for CD with link to AB-01",
          children: [],
          norms: [],
          linkedFields: ["AB-01"],
          isExpanded: false,
          hasChildren: false,
        },
      ],
    })
  const getChildrenOfPR = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: true,
          identifier: "PR-05",
          text: "Beendigung der Phantasieverhältnisse",
          linkedFields: [],
          norms: [
            {
              abbreviation: "PStG",
              singleNormDescription: "§ 99",
            },
          ],
          children: [],
          parent: {
            hasChildren: true,
            identifier: "PR",
            text: "Phantasierecht",
            linkedFields: [],
            norms: [],
            children: [],
            parent: undefined,
          },
        },
      ],
    })
  const getChildrenOfPRO5 = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: false,
          identifier: "PR-05-01",
          text: "Phantasie besonderer Art, Ansprüche anderer Art",
          norms: [],
          children: [],
          parent: {
            hasChildren: true,
            identifier: "PR-05",
            text: "Beendigung der Phantasieverhältnisse",
            linkedFields: [],
            norms: [
              {
                abbreviation: "PStG",
                singleNormDescription: "§ 99",
              },
            ],
            children: [],
            parent: {
              hasChildren: true,
              identifier: "PR",
              text: "Phantasierecht",
              norms: [],
              children: [],
            },
          },
        },
      ],
    })
  const getChildrenOfPR0501 = () =>
    Promise.resolve({
      status: 200,
      data: [
        {
          hasChildren: false,
          identifier: "PR-05-01",
          text: "Phantasie besonderer Art, Ansprüche anderer Art",
          norms: [],
          children: [],
          parent: {
            hasChildren: true,
            identifier: "PR-05",
            text: "Beendigung der Phantasieverhältnisse",
            linkedFields: [],
            norms: [
              {
                abbreviation: "PStG",
                singleNormDescription: "§ 99",
              },
            ],
            children: [],
            parent: {
              hasChildren: true,
              identifier: "PR",
              text: "Phantasierecht",
              norms: [],
              children: [],
            },
          },
        },
      ],
    })
  const getParentAndChildrenForIdentifierPR05 = () =>
    Promise.resolve({
      status: 200,
      data: {
        hasChildren: true,
        identifier: "PR-05",
        text: "Beendigung der Phantasieverhältnisse",
        norms: [
          {
            abbreviation: "PStG",
            singleNormDescription: "§ 99",
          },
        ],
        children: [
          {
            hasChildren: false,
            identifier: "PR-05-01",
            text: "Phantasie besonderer Art, Ansprüche anderer Art",
            norms: [],
            children: [],
            parent: {
              hasChildren: true,
              identifier: "PR-05",
              text: "Beendigung der Phantasieverhältnisse",
              linkedFields: [],
              norms: [
                {
                  abbreviation: "PStG",
                  singleNormDescription: "§ 99",
                },
              ],
              children: [],
              parent: {
                hasChildren: true,
                identifier: "PR",
                text: "Phantasierecht",
                norms: [],
                children: [],
              },
            },
          },
        ],
        parent: {
          id: "a785fb96-a45d-4d4c-8d9c-92d8a6592b22",
          hasChildren: true,
          identifier: "PR",
          text: "Phantasierecht",
          norms: [],
          children: [],
        },
      },
    })
  const searchForFieldsOfLawForPR05 = () =>
    Promise.resolve({
      status: 200,
      data: {
        content: [
          {
            hasChildren: true,
            identifier: "PR-05",
            text: "Beendigung der Phantasieverhältnisse",
            norms: [
              {
                abbreviation: "PStG",
                singleNormDescription: "§ 99",
              },
            ],
            children: [],
            parent: {
              id: "a785fb96-a45d-4d4c-8d9c-92d8a6592b22",
              hasChildren: true,
              identifier: "PR",
              text: "Phantasierecht",
              norms: [],
              children: [],
            },
          },
          {
            hasChildren: false,
            identifier: "PR-05-01",
            text: "Phantasie besonderer Art, Ansprüche anderer Art",
            norms: [],
            children: [],
            parent: {
              hasChildren: true,
              identifier: "PR-05",
              text: "Beendigung der Phantasieverhältnisse",
              norms: [
                {
                  abbreviation: "PStG",
                  singleNormDescription: "§ 99",
                },
              ],
              children: [],
              parent: {
                id: "a785fb96-a45d-4d4c-8d9c-92d8a6592b22",
                hasChildren: true,
                identifier: "PR",
                text: "Phantasierecht",
                norms: [],
                children: [],
              },
            },
          },
        ],
        size: 2,
        number: 0,
        numberOfElements: 2,
        first: true,
        last: true,
        empty: false,
      },
    })
  const searchForFieldsOfLawFail = () =>
    Promise.resolve({
      status: 500,
      error: {
        title: "Something went wrong",
      },
    })

  beforeEach(() => {
    vi.spyOn(FieldOfLawService, "getChildrenOf").mockImplementation(
      (identifier: string) => {
        if (identifier == "root") return getChildrenOfRoot()
        else if (identifier == "PR") return getChildrenOfPR()
        else if (identifier == "PR-05") return getChildrenOfPRO5()
        return getChildrenOfPR0501()
      },
    )
    vi.spyOn(FieldOfLawService, "getTreeForIdentifier").mockImplementation(
      () => {
        return getParentAndChildrenForIdentifierPR05()
      },
    )
    vi.spyOn(FieldOfLawService, "searchForFieldsOfLaw").mockImplementation(
      () => {
        return searchForFieldsOfLawForPR05()
      },
    )
  })

  it("Shows button Sachgebiete", async () => {
    // given when
    renderComponent()

    // then
    expect(
      screen.getByRole("button", { name: "Sachgebiete" }),
    ).toBeInTheDocument()
  })

  it("Shows Radio group when clicking Sachgebiete button", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))

    // then
    expect(screen.getByLabelText("Suche")).toBeInTheDocument()
  })

  it("Shows error message when no search term is entered", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )

    // then
    expect(
      screen.getByText("Geben Sie mindestens ein Suchkriterium ein"),
    ).toBeInTheDocument()
  })

  it("Lists search results", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.type(screen.getByLabelText("Sachgebietskürzel"), "PR-05")
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )

    // then
    await waitFor(() => {
      expect(
        screen.getAllByText("Beendigung der Phantasieverhältnisse")[0],
      ).toBeInTheDocument()
    })
  })

  it("Shows norms when required", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.type(screen.getByLabelText("Sachgebietskürzel"), "PR-05")
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )
    await user.click(screen.getAllByRole("checkbox")[0])

    // then
    await waitFor(() => {
      expect(screen.getByText("§ 99")).toBeInTheDocument()
    })
  })

  it("Shows warning when backend responds with error message", async () => {
    // given
    vi.spyOn(FieldOfLawService, "searchForFieldsOfLaw").mockImplementation(
      () => {
        return searchForFieldsOfLawFail()
      },
    )
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.type(
      screen.getByLabelText("Sachgebietskürzel"),
      "this triggers an error",
    )
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )

    // then
    await waitFor(() => {
      expect(
        screen.getByText(
          "Leider ist ein Fehler aufgetreten. Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut.",
        ),
      ).toBeInTheDocument()
    })
  })

  it("Resets search results", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.type(screen.getByLabelText("Sachgebietskürzel"), "PR-05")
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )
    await waitFor(() => {
      expect(
        screen.getAllByText("Beendigung der Phantasieverhältnisse")[0],
      ).toBeInTheDocument()
    })
    await user.click(screen.getByRole("button", { name: "Suche zurücksetzen" }))

    // then
    expect(
      screen.queryByText("Beendigung der Phantasieverhältnisse"),
    ).not.toBeInTheDocument()
  })

  it("Adds a field of law to the selection", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.type(screen.getByLabelText("Sachgebietskürzel"), "PR-05")
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )
    await waitFor(() => {
      expect(
        screen.getAllByText("Beendigung der Phantasieverhältnisse")[0],
      ).toBeInTheDocument()
    })
    await user.click(screen.getByLabelText("PR-05 hinzufügen"))

    // then
    expect(
      screen.getByRole("button", {
        name: "PR-05 Beendigung der Phantasieverhältnisse aus Liste entfernen",
      }),
    ).toBeInTheDocument()
  })

  it("Cannot add a field of law to the selection twice", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.type(screen.getByLabelText("Sachgebietskürzel"), "PR-05")
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )
    await waitFor(() => {
      expect(
        screen.getAllByText("Beendigung der Phantasieverhältnisse")[0],
      ).toBeInTheDocument()
    })
    await user.click(screen.getByLabelText("PR-05 hinzufügen"))
    await user.click(screen.getByLabelText("PR-05 hinzufügen"))

    // then
    expect(
      screen.getAllByRole("button", {
        name: "PR-05 Beendigung der Phantasieverhältnisse aus Liste entfernen",
      }).length,
    ).toBe(1)
  })

  it("Remove a selected field of law", async () => {
    // given
    const { user } = renderComponent()

    // when
    await user.click(screen.getByRole("button", { name: "Sachgebiete" }))
    await user.click(screen.getByLabelText("Suche"))
    await user.type(screen.getByLabelText("Sachgebietskürzel"), "PR-05")
    await user.click(
      screen.getByRole("button", { name: "Sachgebietssuche ausführen" }),
    )
    await waitFor(() => {
      expect(
        screen.getAllByText("Beendigung der Phantasieverhältnisse")[0],
      ).toBeInTheDocument()
    })
    await user.click(screen.getByLabelText("PR-05 hinzufügen"))
    await user.click(
      screen.getByRole("button", {
        name: "PR-05 Beendigung der Phantasieverhältnisse aus Liste entfernen",
      }),
    )

    // then
    expect(
      screen.queryByRole("button", {
        name: "PR-05 Beendigung der Phantasieverhältnisse aus Liste entfernen",
      }),
    ).not.toBeInTheDocument()
  })
})
