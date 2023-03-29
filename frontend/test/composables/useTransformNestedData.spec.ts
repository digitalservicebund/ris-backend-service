import { ref } from "vue"
import { InputField, InputType } from "@/shared/components/input/types"
import { useTransformNestedData } from "@/shared/composables/useTransformNestedData"

const data = ref({
  testKey1: "testValue1",
  testKey2: "testValue2",
  testKey3: "testValue3",
  testKey4: "testValue4",
})

const oneField: InputField[] = [
  {
    name: "nestedInputOfTestKey1AndTestKey2",
    type: InputType.NESTED,
    inputAttributes: {
      ariaLabel: "baz",
      fields: {
        parent: {
          name: "testKey1",
          type: InputType.TEXT,
          inputAttributes: { ariaLabel: "foo" },
        },
        child: {
          name: "testKey2",
          type: InputType.TEXT,
          inputAttributes: { ariaLabel: "bar" },
        },
      },
    },
  },
]

const twoFields = [
  ...oneField,
  {
    name: "nestedInputOfTestKey3AndTestKey4",
    type: InputType.NESTED,
    inputAttributes: {
      ariaLabel: "baz",
      fields: {
        parent: {
          name: "testKey3",
          type: InputType.TEXT,
          inputAttributes: { ariaLabel: "foo" },
        },
        child: {
          name: "testKey4",
          type: InputType.TEXT,
          inputAttributes: { ariaLabel: "bar" },
        },
      },
    },
  } as InputField,
]

describe("useTransformNestedData", () => {
  it("correctly transforms nested inputs based on model value property", () => {
    const values = useTransformNestedData(data, oneField, vi.fn())

    expect(values.value).toEqual({
      nestedInputOfTestKey1AndTestKey2: {
        fields: { parent: "testValue1", child: "testValue2" },
      },
      testKey3: "testValue3",
      testKey4: "testValue4",
    })
  })

  it("correctly transforms multiple nested inputs based on model value property", () => {
    const values = useTransformNestedData(data, twoFields, vi.fn())

    expect(values.value).toEqual({
      nestedInputOfTestKey1AndTestKey2: {
        fields: { parent: "testValue1", child: "testValue2" },
      },
      nestedInputOfTestKey3AndTestKey4: {
        fields: { parent: "testValue3", child: "testValue4" },
      },
    })
  })

  it("transforms manipulated data back to flat data structure", () => {
    const emit = vi.fn()

    const values = useTransformNestedData(data, twoFields, emit)

    values.value = {
      nestedInputOfTestKey1AndTestKey2: {
        fields: {
          parent: "newTestValue1",
          child: "newTestValue2",
        },
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
