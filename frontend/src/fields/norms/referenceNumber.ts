import { defineChipsField } from "@/fields/caselaw"
import { InputField } from "@/shared/components/input/types"

export const referenceNumber: InputField[] = [
  defineChipsField("referenceNumber", "Aktenzeichen", "Aktenzeichen"),
]
