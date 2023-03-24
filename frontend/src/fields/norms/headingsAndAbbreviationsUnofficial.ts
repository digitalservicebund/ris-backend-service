import { defineChipsField, InputField } from "@/domain"

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
