import { ref } from "vue"
import { useTransformTupleData } from "@/composables/useTransformTupleData"
import { InputField, InputType } from "@/domain"

const data = ref({
  testKey1: "testValue1",
  testKey2: "testValue2",
  testKey3: "testValue3",
  testKey4: "testValue4",
})

const oneField: InputField[] = [
  {
    name: "tupleOfTestKey1AndTestKey2",
    type: InputType.TUPLE,
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
    name: "tupleOfTestKey3AndTestKey4",
    type: InputType.TUPLE,
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

describe("useTransformTupleData", () => {
  it("correctly transforms tuple based on model value property", () => {
    const values = useTransformTupleData(data, oneField, vi.fn())

    expect(values.value).toEqual({
      tupleOfTestKey1AndTestKey2: {
        fields: { parent: "testValue1", child: "testValue2" },
      },
      testKey3: "testValue3",
      testKey4: "testValue4",
    })
  })

  it("correctly transforms multiple tuple based on model value property", () => {
    const values = useTransformTupleData(data, twoFields, vi.fn())

    expect(values.value).toEqual({
      tupleOfTestKey1AndTestKey2: {
        fields: { parent: "testValue1", child: "testValue2" },
      },
      tupleOfTestKey3AndTestKey4: {
        fields: { parent: "testValue3", child: "testValue4" },
      },
    })
  })

  it("transforms manipulated data back to flat data structure", () => {
    const emit = vi.fn()

    const values = useTransformTupleData(data, twoFields, emit)

    values.value = {
      tupleOfTestKey1AndTestKey2: {
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
