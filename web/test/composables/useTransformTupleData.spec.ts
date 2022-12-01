import { ref } from "vue"
import { useTransformTupleData } from "@/composables/useTransformTupleData"

const data = ref({
  testKey1: "testValue1",
  testKey2: "testValue2",
  testKey3: "testValue3",
  testKey4: "testValue4",
})

describe("useTransformTupleData", () => {
  it("correctly transforms tuple based on model value property", () => {
    const values = useTransformTupleData(
      data,
      [{ parentKey: "testKey1", childKey: "testKey2" }],
      vi.fn()
    )

    expect(values.value).toEqual({
      testKey1AndTestKey2: { parent: "testValue1", child: "testValue2" },
      testKey3: "testValue3",
      testKey4: "testValue4",
    })
  })

  it("correctly transforms multiple tuple based on model value property", () => {
    const values = useTransformTupleData(
      data,
      [
        { parentKey: "testKey1", childKey: "testKey2" },
        { parentKey: "testKey3", childKey: "testKey4" },
      ],
      vi.fn()
    )

    expect(values.value).toEqual({
      testKey1AndTestKey2: { parent: "testValue1", child: "testValue2" },
      testKey3AndTestKey4: { parent: "testValue3", child: "testValue4" },
    })
  })

  it("transforms manipulated data back to flat data structure", () => {
    const emit = vi.fn()
    const values = useTransformTupleData(
      data,
      [
        { parentKey: "testKey1", childKey: "testKey2" },
        { parentKey: "testKey3", childKey: "testKey4" },
      ],
      emit
    )

    values.value = {
      testKey1AndTestKey2: {
        parent: "newTestValue1",
        child: "newTestValue2",
      },
      testKey3: "newTestValue3",
    }
    expect(emit).toHaveBeenCalledOnce()
    expect(emit).toHaveBeenLastCalledWith("update:modelValue", {
      testKey1: "newTestValue1",
      testKey2: "newTestValue2",
      testKey3: "newTestValue3",
    })
  })
})
