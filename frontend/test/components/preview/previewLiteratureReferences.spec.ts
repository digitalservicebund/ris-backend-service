import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewLiteratureReferences from "@/components/preview/PreviewLiteratureReferences.vue"
import Reference from "@/domain/reference"

function renderComponent(literatureReferences?: Reference[]) {
  return render(PreviewLiteratureReferences, {
    props: {
      literatureReferences: literatureReferences || [],
    },
    global: {
      provide: {
        [previewLayoutInjectionKey as symbol]: "wide",
      },
    },
  })
}

describe("preview literature references", () => {
  test.each([undefined, []])(
    "exclude references if null or empty",
    async (references?: Reference[]) => {
      renderComponent(references)
      expect(
        screen.queryByTestId("literature-references-preview"),
      ).not.toBeInTheDocument()
    },
  )

  it.each([
    [
      "LiteraturfundstellenABC 2006, S.3, foo (Ean)",
      [
        new Reference({
          citation: "2006, S.3",
          author: "foo",
          documentType: { label: "fda", jurisShortcut: "Ean" },
          legalPeriodical: {
            abbreviation: "ABC",
          },
        }),
      ],
    ],
    [
      "LiteraturfundstellenABC 2006, S.3XYZ 2007, S.4",
      [
        new Reference({
          citation: "2006, S.3",
          legalPeriodical: {
            abbreviation: "ABC",
          },
        }),
        new Reference({
          citation: "2007, S.4",
          legalPeriodical: {
            abbreviation: "XYZ",
          },
        }),
      ],
    ],
  ])(
    `renders primary references %s in preview`,
    async (expected: string, references: Reference[]) => {
      renderComponent(references)
      expect(
        screen.getByTestId("literature-references-preview"),
      ).toBeInTheDocument()
      expect(
        screen.getByTestId("literature-references-preview"),
      ).toHaveTextContent(expected)
    },
  )
})
