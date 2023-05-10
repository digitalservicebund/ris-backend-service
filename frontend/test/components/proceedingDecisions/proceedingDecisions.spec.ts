import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentUnitProceedingDecisions from "@/components/proceedingDecisions/ProceedingDecisions.vue"
import { ProceedingDecision } from "@/domain/proceedingDecision"
import documentUnitService from "@/services/documentUnitService"
import proceedingDecisionService from "@/services/proceedingDecisionService"

function renderComponent(options?: {
  documentUnitUuid?: string
  proceedingDecisions?: ProceedingDecision[]
}) {
  const props = {
    documentUnitUuid: options?.documentUnitUuid
      ? options?.documentUnitUuid
      : "fooUuid",
    proceedingDecisions: options?.proceedingDecisions
      ? options?.proceedingDecisions
      : [],
  }

  const user = userEvent.setup()
  return {
    user,
    ...render(DocumentUnitProceedingDecisions, {
      props,
      global: {
        stubs: { routerLink: { template: "<a><slot/></a>" } },
      },
    }),
  }
}

async function openExpandableArea(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByText("Vorgehende Entscheidungen"))
}

describe("DocumentUnitProceedingDecisions", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")

  it("shows all proceeding decision input fields if expanded", async () => {
    const { user } = renderComponent()
    await openExpandableArea(user)

    expect(screen.getByLabelText("Gericht Rechtszug")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Rechtszug")
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Aktenzeichen Rechtszug")).toBeInTheDocument()
    expect(screen.getByLabelText("Dokumenttyp Rechtszug")).toBeInTheDocument()
  })

  it("creates new proceeding decision", async () => {
    const fetchSpy = vi
      .spyOn(proceedingDecisionService, "createProceedingDecision")
      .mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: [
            new ProceedingDecision({
              ...{
                court: {
                  type: "type1",
                  location: "location1",
                  label: "label1",
                },
                date: "2022-02-01",
                documentType: {
                  jurisShortcut: "documentTypeShortcut1",
                  label: "documentType1",
                },
                fileNumber: "testFileNumber1",
              },
            }),
          ],
        })
      )
    const { user } = renderComponent()
    await openExpandableArea(user)

    expect(screen.queryByText(/testFileNumber1/)).not.toBeInTheDocument()

    await user.type(
      await screen.findByLabelText("Aktenzeichen Rechtszug"),
      "testFileNumber1"
    )
    await user.click(screen.getByLabelText("Entscheidung manuell hinzufügen"))
    expect(fetchSpy).toBeCalledTimes(1)

    expect(screen.getByText(/testFileNumber1/)).toBeVisible()
  })

  it("does not create decision if undefined", async () => {
    const fetchSpy = vi.spyOn(
      proceedingDecisionService,
      "createProceedingDecision"
    )
    const { user } = renderComponent()
    await openExpandableArea(user)

    await user.click(screen.getByLabelText("Entscheidung manuell hinzufügen"))
    expect(fetchSpy).toBeCalledTimes(0)
  })

  it("lists search results", async () => {
    const fetchSpy = vi
      .spyOn(documentUnitService, "searchByProceedingDecisionInput")
      .mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: [
            new ProceedingDecision({
              ...{
                court: {
                  type: "type1",
                  location: "location1",
                  label: "label1",
                },
                date: "2022-02-01",
                documentType: {
                  jurisShortcut: "documentTypeShortcut1",
                  label: "documentType1",
                },
                fileNumber: "test fileNumber",
              },
            }),
            new ProceedingDecision({
              ...{
                court: {
                  type: "type2",
                  location: "location2",
                  label: "label2",
                },
                date: "2022-02-02",
                documentType: {
                  jurisShortcut: "documentTypeShortcut2",
                  label: "documentType2",
                },
                fileNumber: "test fileNumber",
              },
            }),
          ],
        })
      )
    const { user } = renderComponent()
    await openExpandableArea(user)

    expect(screen.queryByText(/test fileNumber/)).not.toBeInTheDocument()

    await user.type(
      await screen.findByLabelText("Aktenzeichen Rechtszug"),
      "test fileNumber"
    )
    await user.click(screen.getByLabelText("Nach Entscheidungen suchen"))
    expect(fetchSpy).toBeCalledTimes(1)

    expect(screen.getAllByText(/test fileNumber/).length).toBe(2)
  })

  it("adds proceeding decision from search results and updates indicators"),
    async () => {
      const fetchSpy = vi
        .spyOn(documentUnitService, "searchByProceedingDecisionInput")
        .mockImplementation(() =>
          Promise.resolve({
            status: 200,
            data: [
              new ProceedingDecision({
                ...{
                  court: {
                    type: "type1",
                    location: "location1",
                    label: "label1",
                  },
                  date: "2022-02-01",
                  documentType: {
                    jurisShortcut: "documentTypeShortcut1",
                    label: "documentType1",
                  },
                  fileNumber: "test fileNumber",
                },
              }),
            ],
          })
        )
      const { user } = renderComponent()
      await openExpandableArea(user)

      await user.click(screen.getByLabelText("Nach Entscheidungen suchen"))
      expect(fetchSpy).toBeCalledTimes(1)

      expect(screen.getByText(/test fileNumber/)).toBeVisible()
      await user.click(screen.getByLabelText("Treffer übernehmen"))
      expect(screen.getByText(/Bereits hinzugefügt/)).toBeVisible()
      expect(screen.getAllByText(/test fileNumber/).length).toBe(2)
    }
})
