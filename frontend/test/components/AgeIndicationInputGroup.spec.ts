import { render, screen } from "@testing-library/vue"
import AgeIndicationInputGroup from "@/components/AgeIndicationInputGroup.vue"
import { Metadata, MetadatumType, RangeUnit } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(AgeIndicationInputGroup, { props })
}

describe("AgeIndicationInputGroup", () => {
  it("renders an input field for the Starting Age value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.RANGE_START]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Anfang",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  describe("Range start unit dropdown", () => {
    const testCases = [
      {
        modelValue: { [MetadatumType.RANGE_START_UNIT]: [RangeUnit.YEARS] },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Jahr",
      },
      {
        modelValue: { [MetadatumType.RANGE_START_UNIT]: [RangeUnit.MONTHS] },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Monat",
      },
      {
        modelValue: { [MetadatumType.RANGE_START_UNIT]: [RangeUnit.WEEKS] },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Woche",
      },
      {
        modelValue: { [MetadatumType.RANGE_START_UNIT]: [RangeUnit.DAYS] },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Tag",
      },
      {
        modelValue: { [MetadatumType.RANGE_START_UNIT]: [RangeUnit.HOURS] },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Stunde",
      },
      {
        modelValue: { [MetadatumType.RANGE_START_UNIT]: [RangeUnit.MINUTES] },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Minute",
      },
      {
        modelValue: { [MetadatumType.RANGE_START_UNIT]: [RangeUnit.SECONDS] },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Sekunde",
      },
      {
        modelValue: {
          [MetadatumType.RANGE_START_UNIT]: [RangeUnit.YEARS_OF_LIFE],
        },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Lebensjahre",
      },
      {
        modelValue: {
          [MetadatumType.RANGE_START_UNIT]: [RangeUnit.MONTHS_OF_LIFE],
        },
        expectedLabel: "Altersangabe Starteinheit",
        expectedValue: "Lebensmonate",
      },
    ]

    testCases.forEach((testCase) => {
      it(`should render a dropdown field with the correct label and value for the RANGE_START_UNIT metadatum`, async () => {
        renderComponent({
          modelValue: testCase.modelValue,
        })

        const dropdown = screen.getByLabelText(
          testCase.expectedLabel
        ) as HTMLSelectElement

        expect(dropdown).toBeVisible()
        expect(dropdown).toHaveValue(testCase.expectedValue)
      })
    })
  })

  describe("Range end unit dropdown rendering", () => {
    const testCases = [
      {
        modelValue: { [MetadatumType.RANGE_END_UNIT]: [RangeUnit.YEARS] },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Jahr",
      },
      {
        modelValue: { [MetadatumType.RANGE_END_UNIT]: [RangeUnit.MONTHS] },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Monat",
      },
      {
        modelValue: { [MetadatumType.RANGE_END_UNIT]: [RangeUnit.WEEKS] },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Woche",
      },
      {
        modelValue: { [MetadatumType.RANGE_END_UNIT]: [RangeUnit.DAYS] },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Tag",
      },
      {
        modelValue: { [MetadatumType.RANGE_END_UNIT]: [RangeUnit.HOURS] },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Stunde",
      },
      {
        modelValue: { [MetadatumType.RANGE_END_UNIT]: [RangeUnit.MINUTES] },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Minute",
      },
      {
        modelValue: { [MetadatumType.RANGE_END_UNIT]: [RangeUnit.SECONDS] },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Sekunde",
      },
      {
        modelValue: {
          [MetadatumType.RANGE_END_UNIT]: [RangeUnit.YEARS_OF_LIFE],
        },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Lebensjahre",
      },
      {
        modelValue: {
          [MetadatumType.RANGE_END_UNIT]: [RangeUnit.MONTHS_OF_LIFE],
        },
        expectedLabel: "Altersangabe Endgerät",
        expectedValue: "Lebensmonate",
      },
    ]

    testCases.forEach((testCase) => {
      it(`should render dropdown field with correct label and value for the RANGE_END_UNIT metadatum when modelValue is ${JSON.stringify(
        testCase.modelValue
      )}`, async () => {
        renderComponent({
          modelValue: testCase.modelValue,
        })

        const dropdown = screen.getByLabelText(
          testCase.expectedLabel
        ) as HTMLSelectElement

        expect(dropdown).toBeVisible()
        expect(dropdown).toHaveValue(testCase.expectedValue)
      })
    })
  })

  it("renders an input field for the End Age value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.RANGE_END]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Ende",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })
})
