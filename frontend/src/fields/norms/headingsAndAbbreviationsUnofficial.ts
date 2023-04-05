import { defineChipsField } from "@/fields/caselaw"
import { InputField } from "@/shared/components/input/types"

export const headingsAndAbbreviationsUnofficial: InputField[] = [
  defineChipsField(
    "unofficialLongTitle",
    "unofficialLongTitle",
    "Nichtamtliche Langüberschrift",
    "Nichtamtliche Langüberschrift"
  ),
  defineChipsField(
    "unofficialShortTitle",
    "unofficialShortTitle",
    "Nichtamtliche Kurzüberschrift",
    "Nichtamtliche Kurzüberschrift"
  ),
  defineChipsField(
    "unofficialAbbreviation",
    "unofficialAbbreviation",
    "Nichtamtliche Buchstabenabkürzung",
    "Nichtamtliche Buchstabenabkürzung"
  ),
]
