import { Ref, computed } from "vue"
import { ModelType } from "@/domain/types"

type FieldData = { [fieldName: string]: ModelType }

function getTupleKey(parentKey: string, childKey: string) {
  const sentenceCaseChildKey =
    childKey.charAt(0).toUpperCase() + childKey.slice(1)
  return `${parentKey}And${sentenceCaseChildKey}`
}

//TODO Naming to be done
function transformToNestedData(
  data: Record<string, ModelType>,
  parentKey: string,
  childKey: string
): Record<string, ModelType> {
  const nestedData = { ...data }
  delete nestedData[parentKey]
  delete nestedData[childKey]
  const tupleKey = getTupleKey(parentKey, childKey)
  Object.assign(nestedData, {
    [tupleKey]: {
      parent: data[parentKey],
      child: data[childKey],
    },
  })
  return nestedData
}

function transformToFlatData(
  data: Record<string, ModelType>,
  parentKey: string,
  childKey: string
) {
  const tupleKey = getTupleKey(parentKey, childKey)
  const tupleValue = data[tupleKey]
  if (
    typeof tupleValue === "object" &&
    "parent" in tupleValue &&
    "child" in tupleValue
  ) {
    const flattenData = { ...data }
    delete flattenData[tupleKey]
    flattenData[parentKey] = tupleValue.parent
    flattenData[childKey] = tupleValue.child
    return flattenData
  } else throw new Error(`Can not flatten tuple key: ${tupleKey}`)
}

interface Emits {
  (event: "update:modelValue", value: FieldData): void
}

export function useTransformTupleData<E extends Emits>(
  data: Ref<FieldData>,
  tuples: { parentKey: string; childKey: string }[],
  emit: E
) {
  return computed({
    get: () => {
      let nestedData = data.value
      tuples.map((tuple) => {
        nestedData = transformToNestedData(
          nestedData,
          tuple.parentKey,
          tuple.childKey
        )
      })
      return nestedData
    },
    set: (newValues) => {
      let flatData = data.value
      tuples.map((tuple) => {
        flatData = transformToFlatData(
          newValues,
          tuple.parentKey,
          tuple.childKey
        )
      })
      emit("update:modelValue", flatData)
    },
  })
}
