import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ActiveCitations from "@/components/ActiveCitations.vue"
import ActiveCitation from "@/domain/activeCitation"
import documentUnitService from "@/services/documentUnitService"

function renderComponent(options?: { modelValue?: ActiveCitation[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  const user = userEvent.setup()
  return {
    user,
    ...render(ActiveCitations, {
      props,
      global: {
        stubs: { routerLink: { template: "<a><slot/></a>" } },
      },
    }),
  }
}

async function openExpandableArea(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByText("Aktivzitierung"))
}

describe("Active Citations", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")

  it("shows all input fields if expanded", async () => {
    const { user } = renderComponent()
    await openExpandableArea(user)

    expect(screen.getByLabelText("Art der Zitierung")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung")
    ).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung")
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Aktenzeichen Aktivzitierung")
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp Aktivzitierung")
    ).toBeInTheDocument()
  })

  it("creates new active citation", async () => {
    //todo
  })

  it("does not create active citation if undefined", async () => {
    //todo
  })

  it("lists search results", async () => {
    const fetchSpy = vi
      .spyOn(documentUnitService, "searchByLinkedDocumentUnit")
      .mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: {
            content: [
              new ActiveCitation({
                court: {
                  type: "type1",
                  location: "location1",
                  label: "label1",
                },
                decisionDate: "2022-02-01",
                documentType: {
                  jurisShortcut: "documentTypeShortcut1",
                  label: "documentType1",
                },
                fileNumber: "test fileNumber",
              }),
              new ActiveCitation({
                court: {
                  type: "type2",
                  location: "location2",
                  label: "label2",
                },
                decisionDate: "2022-02-02",
                documentType: {
                  jurisShortcut: "documentTypeShortcut2",
                  label: "documentType2",
                },
                fileNumber: "test fileNumber",
              }),
            ],
            size: 0,
            totalElements: 20,
            totalPages: 2,
            number: 0,
            numberOfElements: 20,
            first: true,
            last: false,
          },
        })
      )
    const { user } = renderComponent()
    await openExpandableArea(user)

    expect(screen.queryByText(/test fileNumber/)).not.toBeInTheDocument()

    await user.type(
      await screen.findByLabelText("Aktenzeichen Aktivzitierung"),
      "test fileNumber"
    )
    await user.click(screen.getByLabelText("Nach Entscheidungen suchen"))
    expect(fetchSpy).toBeCalledTimes(1)

    expect(screen.getAllByText(/test fileNumber/).length).toBe(2)
  })

  it("adds proceeding decision from search results and updates indicators"),
    async () => {
      //todo
    }
})
