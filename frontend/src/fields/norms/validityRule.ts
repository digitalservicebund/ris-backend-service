import { defineChipsField } from "@/fields/caselaw"
import { InputField } from "@/shared/components/input/types"

export const validityRule: InputField[] = [
  defineChipsField(
    "validityRule",
    "validityRule",
    "Gültigkeitsregelung",
    "Gültigkeitsregelung"
  ),
]
