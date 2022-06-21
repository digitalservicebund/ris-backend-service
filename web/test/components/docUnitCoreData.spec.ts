import { createTestingPinia } from "@pinia/testing"
import { mount } from "@vue/test-utils"
import { describe, test, expect } from "vitest"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocUnitCoreData from "../../src/components/DocUnitCoreData.vue"
import { useDocUnitsStore } from "../../src/store"
import { buildEmptyDocUnit } from "../../src/types/DocUnit"

// vitest run --testNamePattern CoreData
describe("Core Data", () => {
  const vuetify = createVuetify({ components, directives })

  test("renders correctly with given docUnitId", async () => {
    const pinia = createTestingPinia({ stubActions: false })
    const store = useDocUnitsStore()
    const docUnit = buildEmptyDocUnit()
    docUnit.id = "1"
    docUnit.aktenzeichen = "abc"
    store.add(docUnit)
    store.setSelected("1")

    const wrapper = mount(DocUnitCoreData, {
      global: {
        plugins: [vuetify, pinia],
      },
    })

    expect(
      (wrapper.find("#aktenzeichen").element as HTMLInputElement).value
    ).toBe("abc")
    expect(wrapper.get("button").text()).toBe("Speichern")
    // what else? TODO
  })
})
