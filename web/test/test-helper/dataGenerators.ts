import { InputType } from "@/domain"
import type {
  BaseInputAttributes,
  BaseInputField,
  TextInputAttributes,
  TextInputField,
} from "@/domain"

const ALPHABET_CHARACTERS = "abcdefghijklmnopqrstuvwxyz"

export function generateString(options?: {
  characterSet?: string
  length?: number
  prefix?: string
}): string {
  const characterSet = options?.characterSet ?? ALPHABET_CHARACTERS
  const length = options?.length ?? 5
  let output = options?.prefix ?? ""

  for (let i = 0; i < length; i++) {
    output += characterSet.charAt(
      Math.floor(Math.random() * characterSet.length)
    )
  }

  return output
}

export function generateBaseInputAttributes(
  partialAttributes?: Partial<BaseInputAttributes>
): BaseInputAttributes {
  return {
    ariaLabel: generateString({ prefix: "aria-label-" }),
    ...partialAttributes,
  }
}

export function generateTextInputAttributes(
  partialAttributes?: Partial<TextInputAttributes>
): TextInputAttributes {
  return {
    ...generateBaseInputAttributes(),
    placeholder: undefined,
    ...partialAttributes,
  }
}

export function generateBaseInputField(
  partialField?: Partial<BaseInputField>
): BaseInputField {
  return {
    type: InputType.TEXT,
    name: generateString({ prefix: "name-" }),
    label: generateString({ prefix: "Label " }),
    inputAttributes: generateBaseInputAttributes(),
    ...partialField,
  }
}

export function generateTextInputField(
  partialField?: Partial<TextInputField>
): TextInputField {
  return {
    ...generateBaseInputField(),
    type: InputType.TEXT,
    inputAttributes: generateTextInputAttributes(),
    ...partialField,
  }
}
