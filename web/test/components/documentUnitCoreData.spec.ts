import { mount } from "@vue/test-utils"
import DocumentUnit from "../../src/domain/documentUnit"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"

// vitest run --testNamePattern CoreData
describe("Core Data", () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  test("renders correctly with given documentUnitId", async () => {
    const documentUnit = new DocumentUnit("1", {
      coreData: {
        fileNumber: ["abc"],
      },
      documentNumber: "ABCD2022000001",
    })
    const wrapper = mount(DocumentUnitCoreData, {
      props: {
        modelValue: documentUnit.coreData,
        updateStatus: 0,
      },
    })
    //TODO this test, needs to be adjusted when multitext model value is rendered correctly in fileNumber
    expect(
      (wrapper.find("#fileNumber").element as HTMLInputElement).value
    ).toBe("")
    const buttons = wrapper.findAll("button")
    expect(buttons[buttons.length - 1].text()).toBe("Speichern")
    expect(wrapper.text()).toContain("* Pflichtfelder zum Veröffentlichen")
    // what else? TODO
  })
})
