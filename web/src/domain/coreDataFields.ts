import {
  InputType,
  LookupTableEndpoint,
  TupleInputAttributes,
  ValidationError,
} from "./types"
import type { InputField, DropdownItem } from "./types"
import legalEffectTypes from "@/data/legalEffectTypes.json"

export function defineTextField(
  name: string,
  label: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string,
  validationError?: ValidationError,
  readOnly?: boolean
): InputField {
  return {
    name,
    type: InputType.TEXT,
    label,
    required,
    inputAttributes: {
      ariaLabel,
      placeholder,
      validationError,
      readOnly,
    },
  }
}

export function defineMultiTextField(
  name: string,
  label: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string
): InputField {
  return {
    name,
    type: InputType.MULTITEXT,
    label,
    required,
    inputAttributes: {
      ariaLabel,
      placeholder,
    },
  }
}

export function defineDateField(
  name: string,
  label: string,
  ariaLabel: string,
  required?: boolean,
  validationError?: ValidationError
): InputField {
  return {
    name,
    type: InputType.DATE,
    label,
    required,
    inputAttributes: { ariaLabel, validationError },
  }
}

export function defineDropdownField(
  name: string,
  label: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string,
  isCombobox?: boolean,
  dropdownItems?: DropdownItem[],
  endpoint?: LookupTableEndpoint,
  preselectedValue?: string,
  validationError?: ValidationError
): InputField {
  return {
    name,
    type: InputType.DROPDOWN,
    label,
    required,
    inputAttributes: {
      ariaLabel,
      placeholder,
      dropdownItems,
      endpoint,
      isCombobox,
      preselectedValue,
      validationError,
    },
  }
}

export function defineTupleField(
  ariaLabel: string,
  name: string,
  fields: TupleInputAttributes["fields"]
): InputField {
  return {
    name,
    type: InputType.TUPLE,
    inputAttributes: {
      ariaLabel,
      fields,
    },
  }
}

export const coreDataFields: InputField[] = [
  defineDropdownField(
    "court",
    "Gericht",
    "Gericht",
    true,
    "Gerichtstyp Gerichtsort",
    true,
    [],
    LookupTableEndpoint.courts
  ),
  defineTupleField(
    "Toggle Abweichendes Aktenzeichen",
    "TupleOfFileNumbersAndDeviatingFileNumbers",
    {
      parent: defineMultiTextField(
        "fileNumbers",
        "Aktenzeichen",
        "Aktenzeichen",
        true,
        ""
      ),
      child: defineMultiTextField(
        "deviatingFileNumbers",
        "Abweichendes Aktenzeichen",
        "Abweichendes Aktenzeichen",
        false,
        ""
      ),
    }
  ),
  defineDateField(
    "decisionDate",
    "Entscheidungsdatum",
    "Entscheidungsdatum",
    true,
    undefined
  ),
  defineTextField("appraisalBody", "Spruchkörper", "Spruchkörper"),
  defineDropdownField(
    "category",
    "Dokumenttyp",
    "Dokumenttyp",
    true,
    "Bitte auswählen",
    true,
    [],
    LookupTableEndpoint.documentTypes
  ),
  defineTextField(
    "ecli",
    "ECLI",
    "ECLI",
    false,
    "",
    { defaultMessage: "", field: "" },
    false
    // defineMultiTextField(
    //   "divergentECLI",
    //   "Abweichender ECLI",
    //   "Abweichender ECLI",
    //   false,
    //   ""
    // )
  ),
  defineTextField("procedure", "Vorgang", "Vorgang"),
  defineDropdownField(
    "legalEffect",
    "Rechtskraft",
    "Rechtskraft",
    true,
    "",
    false,
    legalEffectTypes.items,
    undefined,
    legalEffectTypes.items[0].value
  ),
]

export const prefilledDataFields: InputField[] = [
  defineTextField(
    "center",
    "Dokumentationsstelle",
    "Dokumentationsstelle",
    false,
    "",
    { defaultMessage: "", field: "" },
    true
  ),
  defineTextField(
    "region",
    "Region",
    "Region",
    false,
    "",
    { defaultMessage: "", field: "" },
    true
  ),
  defineTextField(
    "type",
    "Dokumentart",
    "Dokumentart",
    false,
    "",
    { defaultMessage: "", field: "" },
    true
  ),
  defineTextField(
    "judicature",
    "Gerichtbarkeit",
    "Gerichtbarkeit",
    false,
    "",
    { defaultMessage: "", field: "" },
    true
  ),
]

export const moreCategories: InputField[] = [
  defineDropdownField(
    "moreCategories",
    "Weitere Rubrik",
    "Weitere Rubrik",
    false,
    "Bitte auswählen",
    false,
    []
  ),
]
