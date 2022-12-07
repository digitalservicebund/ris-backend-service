import { mount } from "@vue/test-utils"
import DocumentUnit from "../../src/domain/documentUnit"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"

// vitest run --testNamePattern CoreData
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
    const wrapper = mount(DocumentUnitCoreData, {
      props: {
        modelValue: documentUnit.coreData,
        updateStatus: 0,
      },
    })

    expect(
      (wrapper.find("#fileNumbers").element as HTMLInputElement).value
    ).toBe("")

    const chipList = wrapper.findAll(".chip")
    expect(chipList.length).toBe(2)
    expect(wrapper.find("one"))
    expect(wrapper.find("two"))

    expect((wrapper.find("#ecli").element as HTMLInputElement).value).toBe(
      "abc123"
    )
    const buttons = wrapper.findAll("button")
    expect(buttons[buttons.length - 1].text()).toBe("Speichern")
    expect(wrapper.text()).toContain("* Pflichtfelder zum Veröffentlichen")
  })
})
