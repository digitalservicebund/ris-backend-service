import { defineChipsField } from "@/fields/caselaw"
import { InputField } from "@/shared/components/input/types"

export const headingsAndAbbreviationsUnofficial: InputField[] = [
  defineChipsField(
    "unofficialLongTitle",
    "Nichtamtliche Langüberschrift",
    "Nichtamtliche Langüberschrift"
  ),
  defineChipsField(
    "unofficialShortTitle",
    "Nichtamtliche Kurzüberschrift",
    "Nichtamtliche Kurzüberschrift"
  ),
  defineChipsField(
    "unofficialAbbreviation",
    "Nichtamtliche Buchstabenabkürzung",
    "Nichtamtliche Buchstabenabkürzung"
  ),
]
