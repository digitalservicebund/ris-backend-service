import { Ref, computed } from "vue"
import {
  ModelType,
  InputField,
  TupleInputField,
  InputType,
} from "@/domain/types"

type FieldData = { [fieldName: string]: ModelType }

function uppercaseFirstLetter(string: string) {
  return string.charAt(0).toUpperCase() + string.slice(1)
}

function lowercaseFirstLetter(string: string) {
  return string.charAt(0).toLowerCase() + string.slice(1)
}

function getTupleKey(parentKey: string, childKey: string) {
  return (
    "tupleOf" +
    uppercaseFirstLetter(parentKey) +
    "And" +
    uppercaseFirstLetter(childKey)
  )
}

function getKeysFromTupleKey(combinedTupleKey: string) {
  const matches = /^tupleOf(.*)And(.*)/g.exec(combinedTupleKey)
  if (matches)
    return {
      parentKey: lowercaseFirstLetter(matches[1]),
      childKey: lowercaseFirstLetter(matches[2]),
    }
  throw new Error("Could not extract keys from tuple key")
}

function tupalizeData(
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

function flattenData(newValues: Record<string, ModelType>) {
  const flatData = { ...newValues }
  for (const [key, value] of Object.entries(newValues)) {
    if (typeof value === "object" && "parent" in value && "child" in value) {
      const { parentKey, childKey } = getKeysFromTupleKey(key)
      delete flatData[key]
      flatData[parentKey] = value.parent
      flatData[childKey] = value.child
    }
  }
  return flatData
}

interface Emits {
  (event: "update:modelValue", value: FieldData): void
}

export function useTransformTupleData<E extends Emits>(
  data: Ref<FieldData>,
  fields: InputField[],
  emit: E
) {
  return computed({
    get: () => {
      let nestedData = data.value
      fields
        .filter(
          (field): field is TupleInputField => field.type === InputType.TUPLE
        )
        .map((tuple) => {
          nestedData = tupalizeData(
            nestedData,
            tuple.inputAttributes.fields.parent.name,
            tuple.inputAttributes.fields.child.name
          )
        })
      return nestedData
    },
    set: (newValues) => {
      const flatData = flattenData(newValues)
      emit("update:modelValue", flatData)
    },
  })
}
