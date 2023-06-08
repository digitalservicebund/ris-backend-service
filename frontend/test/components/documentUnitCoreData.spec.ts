import { render, screen } from "@testing-library/vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocumentUnit, { CoreData } from "@/domain/documentUnit"

function renderComponent(options?: {
  modelValue?: CoreData
  updateStatus?: number
}) {
  const props = {
    modelValue: options?.modelValue,
    updateStatus: options?.updateStatus ?? 0,
  }
  const utils = render(DocumentUnitCoreData, { props })
  return { screen, props, ...utils }
}

describe("Core Data", () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  test("renders correctly with given documentUnitId", async () => {
    const documentUnit = new DocumentUnit("1", {
      coreData: {
        fileNumbers: ["one", "two"],
        ecli: "abc123",
      },
      documentNumber: "ABCD2022000001",
    })

    const { screen } = renderComponent({ modelValue: documentUnit.coreData })

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    expect(screen.getByLabelText("ECLI")).toHaveValue("abc123")
  })
})
