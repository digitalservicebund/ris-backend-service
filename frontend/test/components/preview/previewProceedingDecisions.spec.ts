import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewProceedingDecisions from "@/components/preview/PreviewProceedingDecisions.vue"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"

function renderComponent(
  previousDecisions?: PreviousDecision[],
  ensuingDecisions?: EnsuingDecision[],
) {
  return render(PreviewProceedingDecisions, {
    props: {
      previousDecisions: previousDecisions,
      ensuingDecisions: ensuingDecisions,
    },
    global: {
      provide: {
        [previewLayoutInjectionKey as symbol]: "wide",
      },
    },
  })
}
describe("preview proceeding decisions", () => {
  test("renders all proceeding decisions", async () => {
    renderComponent(
      [
        new PreviousDecision({
          court: {
            type: "type1",
            location: "location1",
            label: "label1",
          },
          documentType: {
            jurisShortcut: "documentTypeShortcut1",
            label: "documentType1",
          },
          fileNumber: "test fileNumber1",
        }),
      ],
      [
        new EnsuingDecision({
          court: {
            type: "type1",
            location: "location1",
            label: "label1",
          },
          documentType: {
            jurisShortcut: "documentTypeShortcut1",
            label: "documentType1",
          },
          fileNumber: "test fileNumber1",
        }),
      ],
    )

    expect(await screen.findByText("Vorinstanz")).toBeInTheDocument()
    expect(
      await screen.findByText("Nachgehende Entscheidungen"),
    ).toBeInTheDocument()
  })

  test("renders multiple previous decisions", async () => {
    renderComponent(
      [
        new PreviousDecision({
          court: {
            type: "AG",
            location: "Aachen",
            label: "AG Aachen",
          },
          documentType: {
            jurisShortcut: "Bes",
            label: "Beschluss",
          },
          fileNumber: "ABC.123",
        }),
        new PreviousDecision({
          court: {
            type: "BVerfG",
            label: "BVerfG Karlsruhe",
          },
          documentType: {
            jurisShortcut: "Urt",
            label: "Urteil",
          },
          fileNumber: "DEF/456",
        }),
      ],
      [],
    )

    expect(await screen.findByText("Vorinstanz")).toBeInTheDocument()
    expect(
      await screen.findByText("AG Aachen, ABC.123, Beschluss"),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("BVerfG Karlsruhe, DEF/456, Urteil"),
    ).toBeInTheDocument()
    expect(
      screen.queryByText("Nachgehende Entscheidungen"),
    ).not.toBeInTheDocument()
  })

  test("renders multiple ensuing decisions", async () => {
    renderComponent(
      [],
      [
        new EnsuingDecision({
          court: {
            type: "AG",
            location: "Aachen",
            label: "AG Aachen",
          },
          documentType: {
            jurisShortcut: "Bes",
            label: "Beschluss",
          },
          fileNumber: "ABC.123",
        }),
        new EnsuingDecision({
          court: {
            type: "BVerfG",
            label: "BVerfG Karlsruhe",
          },
          documentType: {
            jurisShortcut: "Urt",
            label: "Urteil",
          },
          fileNumber: "DEF/456",
        }),
      ],
    )

    expect(
      await screen.findByText("Nachgehende Entscheidungen"),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("nachgehend, AG Aachen, ABC.123, Beschluss"),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("nachgehend, BVerfG Karlsruhe, DEF/456, Urteil"),
    ).toBeInTheDocument()
    expect(screen.queryByText("Vorinstanz")).not.toBeInTheDocument()
  })

  test("renders nothing with empty lists", async () => {
    renderComponent([], [])
    expect(screen.queryByText("Vorinstanz")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Nachgehende Entscheidungen"),
    ).not.toBeInTheDocument()
  })

  test("renders nothing with undefined lists", async () => {
    renderComponent()
    expect(screen.queryByText("Vorinstanz")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Nachgehende Entscheidungen"),
    ).not.toBeInTheDocument()
  })
})
