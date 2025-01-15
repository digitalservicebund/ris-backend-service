import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewReferences from "@/components/preview/PreviewReferences.vue"
import Reference from "@/domain/reference"

function renderComponent(references?: Reference[]) {
  return render(PreviewReferences, {
    props: {
      references: references || [],
    },
    global: {
      provide: {
        [previewLayoutInjectionKey as symbol]: "wide",
      },
    },
  })
}

describe("preview references", () => {
  test.each([undefined, []])(
    "exclude references if null or empty",
    async (references?: Reference[]) => {
      renderComponent(references)
      expect(
        screen.queryByTestId("primary-references-preview"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByTestId("secondary-references-preview"),
      ).not.toBeInTheDocument()
    },
  )

  it.each([
    [
      "Primäre FundstellenABC 2006, S.3",
      [
        new Reference({
          citation: "2006, S.3",
          primaryReference: true,
          legalPeriodical: {
            abbreviation: "ABC",
          },
        }),
      ],
    ],
    [
      "Primäre FundstellenABC 2006, S.3XYZ 2007, S.4",
      [
        new Reference({
          citation: "2006, S.3",
          primaryReference: true,
          legalPeriodical: {
            abbreviation: "ABC",
          },
        }),
        new Reference({
          citation: "2007, S.4",
          primaryReference: true,
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
        screen.getByTestId("primary-references-preview"),
      ).toBeInTheDocument()
      expect(
        screen.queryByTestId("secondary-references-preview"),
      ).not.toBeInTheDocument()
      expect(
        screen.getByTestId("primary-references-preview"),
      ).toHaveTextContent(expected)
    },
  )

  it.each([
    [
      "Sekundäre FundstellenDEF 2008, S.5",
      [
        new Reference({
          citation: "2008, S.5",
          legalPeriodical: {
            abbreviation: "DEF",
            primaryReference: false,
          },
        }),
      ],
    ],
    [
      "Sekundäre FundstellenDEF 2009, S.5DEF 2009, S.8",
      [
        new Reference({
          citation: "2009, S.5",
          legalPeriodical: {
            abbreviation: "DEF",
            primaryReference: false,
          },
        }),
        new Reference({
          citation: "2009, S.8",
          legalPeriodical: {
            abbreviation: "DEF",
            primaryReference: false,
          },
        }),
      ],
    ],
  ])(
    `renders secondary references %s in preview`,
    async (expected: string, references: Reference[]) => {
      renderComponent(references)
      expect(
        screen.queryByTestId("primary-references-preview"),
      ).not.toBeInTheDocument()
      expect(
        screen.getByTestId("secondary-references-preview"),
      ).toBeInTheDocument()
      expect(
        screen.getByTestId("secondary-references-preview"),
      ).toHaveTextContent(expected)
    },
  )
})
