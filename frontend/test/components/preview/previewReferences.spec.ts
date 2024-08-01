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
      expect(screen.queryByTestId("references-preview")).not.toBeInTheDocument()
    },
  )

  it.each([
    [
      "FundstellenAmtliche FundstellenABC, 2006, S.3",
      [
        new Reference({
          citation: "2006, S.3",
          legalPeriodical: {
            legalPeriodicalAbbreviation: "ABC",
            primaryReference: true,
          },
        }),
      ],
    ],
    [
      "FundstellenAmtliche FundstellenABC, 2006, S.3XYZ, 2007, S.4",
      [
        new Reference({
          citation: "2006, S.3",
          legalPeriodical: {
            legalPeriodicalAbbreviation: "ABC",
            primaryReference: true,
          },
        }),
        new Reference({
          citation: "2007, S.4",
          legalPeriodical: {
            legalPeriodicalAbbreviation: "XYZ",
            primaryReference: true,
          },
        }),
      ],
    ],
    [
      "FundstellenAmtliche FundstellenDEF, 2008, S.5",
      [
        new Reference({
          citation: "2008, S.5",
          legalPeriodical: {
            legalPeriodicalAbbreviation: "DEF",
            primaryReference: true,
          },
        }),
      ],
    ],
    [
      "FundstellenAmtliche FundstellenABC, 2006, S.3DEF, 2008, S.5",
      [
        new Reference({
          citation: "2006, S.3",
          legalPeriodical: {
            legalPeriodicalAbbreviation: "ABC",
            primaryReference: true,
          },
        }),
        new Reference({
          citation: "2008, S.5",
          legalPeriodical: {
            legalPeriodicalAbbreviation: "DEF",
            primaryReference: true,
          },
        }),
      ],
    ],
  ])(
    `renders references %s in preview`,
    async (expected: string, references: Reference[]) => {
      renderComponent(references)
      expect(screen.getByTestId("references-preview")).toBeInTheDocument()
      expect(screen.getByTestId("references-preview")).toHaveTextContent(
        expected,
      )
    },
  )
})
