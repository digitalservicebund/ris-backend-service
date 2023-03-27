import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentUnitProceedingDecisions from "@/components/proceedingDecisions/ProceedingDecisions.vue"
import type { ProceedingDecision } from "@/domain/documentUnit"
import service from "@/services/proceedingDecisionService"

function renderComponent(options?: {
  documentUnitUuid?: string
  proceedingDecisions?: ProceedingDecision[] | undefined
}) {
  const props = {
    documentUnitUuid: options?.documentUnitUuid
      ? options?.documentUnitUuid
      : "",
    proceedingDecisions: options?.proceedingDecisions,
  }
  const utils = render(DocumentUnitProceedingDecisions, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("DocumentUnitProceedingDecisions", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  const fetchSpy = vi
    .spyOn(service, "addProceedingDecision")
    .mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          {
            court: { type: "type1", location: "location1", label: "label1" },
            date: "2022-02-01",
            documentType: {
              jurisShortcut: "ca",
              label: "documentType1",
            },
            fileNumber: "test1",
          },
          {
            court: { type: "type2", location: "location2", label: "label2" },
            date: "2022-02-02",
            documentType: {
              jurisShortcut: "ca",
              label: "documentType2",
            },
            fileNumber: "test2",
          },
        ],
      })
    )

  it("shows all proceeding decision input fields", () => {
    renderComponent()

    const court = screen.getByLabelText("Gericht Rechtszug") as HTMLInputElement

    const date = screen.getByLabelText("Datum Rechtszug") as HTMLInputElement

    const fileNumber = screen.getByLabelText(
      "Aktenzeichen Rechtszug"
    ) as HTMLInputElement

    const documentType = screen.getByLabelText(
      "Dokumenttyp Rechtszug"
    ) as HTMLInputElement

    expect(court).toBeInTheDocument()
    expect(date).toBeInTheDocument()
    expect(fileNumber).toBeInTheDocument()
    expect(documentType).toBeInTheDocument()
  })

  it("adds proceeding decision and returns list of existing ones", async () => {
    const { user } = renderComponent()

    await user.click(
      screen.getAllByLabelText(
        "Entscheidung manuell hinzufügen"
      )[0] as HTMLElement
    )

    expect(fetchSpy).toBeCalledTimes(1)
    expect(
      screen.getByText("type1 location1 documentType1 2022-02-01 test1")
    ).toBeInTheDocument()
    expect(
      screen.getByText("type2 location2 documentType2 2022-02-02 test2")
    ).toBeInTheDocument()
  })

  // it("does not emit update model event when inputs are empty and model is empty too", async () => {
  //   const { emitted, user } = renderComponent({
  //     modelValue: undefined,
  //   })
  //   const input = screen.getByLabelText("fileNumber")

  //   // Do anything without changing the inputs.
  //   await user.click(input)

  //   expect(emitted()["update:modelValue"]).toBeUndefined()
  // })

  // it("always shows at least one input group despite empty model list", () => {
  //   renderComponent({ modelValue: [] })

  //   const courtInput = screen.queryByLabelText(
  //     "Gericht Rechtszug"
  //   ) as HTMLInputElement
  //   const dateInput = screen.queryByLabelText(
  //     "Datum Rechtszug"
  //   ) as HTMLInputElement
  //   const identifierInput = screen.queryByLabelText(
  //     "Aktenzeichen Rechtszug"
  //   ) as HTMLInputElement

  //   expect(courtInput).toBeInTheDocument()
  //   expect(courtInput).toHaveDisplayValue("")
  //   expect(dateInput).toBeInTheDocument()
  //   expect(dateInput).toHaveDisplayValue("")
  //   expect(identifierInput).toBeInTheDocument()
  //   expect(identifierInput).toHaveDisplayValue("")
  // })
})
