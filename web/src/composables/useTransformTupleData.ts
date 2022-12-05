import { Ref, computed } from "vue"
import { ModelType } from "@/domain/types"

type FieldData = { [fieldName: string]: ModelType }

function uppercaseFirstLetter(string: string) {
  return string.charAt(0).toUpperCase() + string.slice(1)
}

function lowercaseFirstLetter(string: string) {
  return string.charAt(0).toLowerCase() + string.slice(1)
}

function getTupleKey(parentKey: string, childKey: string) {
  return (
    "TupleOf" +
    uppercaseFirstLetter(parentKey) +
    "And" +
    uppercaseFirstLetter(childKey)
  )
}

function getKeysFromTupleKey(combinedTupleKey: string) {
  const matches = /^TupleOf(.*)And(.*)/g.exec(combinedTupleKey)
  if (matches)
    return {
      parentKey: lowercaseFirstLetter(matches[1]),
      childKey: lowercaseFirstLetter(matches[2]),
    }
  throw new Error("Could not extract keys from tuple key")
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

function transformToFlatData(newValues: Record<string, ModelType>) {
  for (const tupleKey in newValues) {
    const tupleValue = newValues[tupleKey]
    if (
      typeof tupleValue === "object" &&
      "parent" in tupleValue &&
      "child" in tupleValue
    ) {
      const { parentKey, childKey } = getKeysFromTupleKey(tupleKey)

      const flattenData = { ...newValues }
      delete flattenData[tupleKey]
      flattenData[parentKey] = tupleValue.parent
      flattenData[childKey] = tupleValue.child
      return flattenData
      // } else throw new Error(`Can not flatten tuple key: ${tupleKey}`)
    }
  }
  return newValues
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
      const flatData = transformToFlatData(newValues)
      emit("update:modelValue", flatData)
    },
  })
}
