import { ref } from "vue"
import { useTransformTupleData } from "@/composables/useTransformTupleData"

const data = ref({
  testKey1: "testValue1",
  testKey2: "testValue2",
  testKey3: "testValue3",
})

describe("useTransformTupleData", () => {
  it("sets initial input value based on model value property", () => {
    const values = useTransformTupleData(
      data,
      [{ parentKey: "testKey1", childKey: "testKey2" }],
      vi.fn()
    )

    expect(values.value).toEqual({
      testKey3: "testValue3",
      testKey1AndTestKey2: { parent: "testValue1", child: "testValue2" },
    })
  })
})
