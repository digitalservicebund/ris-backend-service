import { mount } from "@vue/test-utils"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocumentUnit from "../../src/domain/documentUnit"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"

// vitest run --testNamePattern CoreData
describe("Core Data", () => {
  const vuetify = createVuetify({ components, directives })

  test("renders correctly with given documentUnitId", async () => {
    const documentUnit = new DocumentUnit("1", {
      coreData: {
        fileNumber: "abc",
      },
      documentNumber: "ABCD2022000001",
    })
    const wrapper = mount(DocumentUnitCoreData, {
      props: {
        modelValue: documentUnit.coreData,
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
