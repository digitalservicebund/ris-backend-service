import { Ref, computed } from "vue"
import {
  ModelType,
  InputField,
  NestedInputField,
  InputType,
} from "@/domain/types"

type FieldData = { [fieldName: string]: ModelType }

function uppercaseFirstLetter(string: string) {
  return string.charAt(0).toUpperCase() + string.slice(1)
}

function lowercaseFirstLetter(string: string) {
  return string.charAt(0).toLowerCase() + string.slice(1)
}

function getNestedInputKey(parentKey: string, childKey: string) {
  return (
    "nestedInputOf" +
    uppercaseFirstLetter(parentKey) +
    "And" +
    uppercaseFirstLetter(childKey)
  )
}

function getKeysFromNestedInputKey(combinedKey: string) {
  const matches = /^nestedInputOf(.*)And(.*)/g.exec(combinedKey)
  if (matches)
    return {
      parentKey: lowercaseFirstLetter(matches[1]),
      childKey: lowercaseFirstLetter(matches[2]),
    }
  throw new Error("Could not extract keys from neste input key")
}

function mapData(
  data: Record<string, ModelType>,
  parentKey: string,
  childKey: string
): Record<string, ModelType> {
  const nestedData = { ...data }
  delete nestedData[parentKey]
  delete nestedData[childKey]
  const key = getNestedInputKey(parentKey, childKey)
  Object.assign(nestedData, {
    [key]: {
      fields: {
        parent: data[parentKey],
        child: data[childKey],
      },
    },
  })
  return nestedData
}

function flattenData(newValues: Record<string, ModelType>) {
  const flatData = { ...newValues }
  for (const [key, value] of Object.entries(newValues)) {
    if (
      typeof value === "object" &&
      "fields" in value &&
      "parent" in value.fields &&
      "child" in value.fields
    ) {
      const { parentKey, childKey } = getKeysFromNestedInputKey(key)
      delete flatData[key]
      flatData[parentKey] = value.fields.parent
      flatData[childKey] = value.fields.child
    }
  }
  return flatData
}

interface Emits {
  (event: "update:modelValue", value: FieldData): void
}

export function useTransformNestedData<E extends Emits>(
  data: Ref<FieldData>,
  fields: InputField[],
  emit: E
) {
  return computed({
    get: () => {
      let nestedData = data.value
      fields
        .filter(
          (field): field is NestedInputField => field.type === InputType.NESTED
        )
        .forEach((item) => {
          nestedData = mapData(
            nestedData,
            item.inputAttributes.fields.parent.name,
            item.inputAttributes.fields.child.name
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
