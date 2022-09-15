import { mount } from "@vue/test-utils"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocUnitCoreData from "../../src/components/DocUnitCoreData.vue"
import DocumentUnit from "../../src/domain/documentUnit"

// vitest run --testNamePattern CoreData
describe("Core Data", () => {
  const vuetify = createVuetify({ components, directives })

  test("renders correctly with given docUnitId", async () => {
    const docUnit = new DocumentUnit("1", {
      fileNumber: "abc",
      documentnumber: "ABCD2022000001",
    })
    const wrapper = mount(DocUnitCoreData, {
      props: {
        modelValue: docUnit.coreData,
        updateStatus: 0,
      },
      global: {
        plugins: [vuetify],
      },
    })

    expect(
      (wrapper.find("#fileNumber").element as HTMLInputElement).value
    ).toBe("abc")
    const buttons = wrapper.findAll("button")
    expect(buttons[buttons.length - 1].text()).toBe("Speichern")
    expect(wrapper.get(".form").element as HTMLDivElement).toHaveTextContent(
      "* Pflichtfelder zum Veröffentlichen"
    )
    // what else? TODO
  })
})
