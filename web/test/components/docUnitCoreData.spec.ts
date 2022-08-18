import { mount } from "@vue/test-utils"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocUnitCoreData from "../../src/components/DocUnitCoreData.vue"
import DocUnit from "../../src/domain/docUnit"

// vitest run --testNamePattern CoreData
describe("Core Data", () => {
  const vuetify = createVuetify({ components, directives })

  test("renders correctly with given docUnitId", async () => {
    const docUnit = new DocUnit("1", {
      aktenzeichen: "abc",
      documentnumber: "ABCD2022000001",
    })
    const wrapper = mount(DocUnitCoreData, {
      props: {
        coreData: docUnit.coreData,
        updateStatus: 0,
      },
      global: {
        plugins: [vuetify],
      },
    })

    expect(
      (wrapper.find("#aktenzeichen").element as HTMLInputElement).value
    ).toBe("abc")
    expect(wrapper.get("button").text()).toBe("Speichern")
    // what else? TODO
  })
})
