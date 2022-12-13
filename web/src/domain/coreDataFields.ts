import {
  InputType,
  LookupTableEndpoint,
  NestedInputAttributes,
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

export function defineChipsField(
  name: string,
  label: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string
): InputField {
  return {
    name,
    type: InputType.CHIPS,
    label,
    required,
    inputAttributes: {
      ariaLabel,
      placeholder,
    },
  }
}

export function defineChipsDateField(
  name: string,
  label: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string
): InputField {
  return {
    name,
    type: InputType.DATECHIPS,
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
      validationError,
    },
  }
}

export function defineNestedInputField(
  ariaLabel: string,
  name: string,
  fields: NestedInputAttributes["fields"]
): InputField {
  return {
    name,
    type: InputType.NESTED,
    inputAttributes: {
      ariaLabel,
      fields,
    },
  }
}

export const courtFields: InputField[] = [
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
]
export const coreDataFields: InputField[] = [
  defineNestedInputField(
    "Abweichendes Aktenzeichen",
    "nestedInputOfFileNumbersAndDeviatingFileNumbers",
    {
      parent: defineChipsField(
        "fileNumbers",
        "Aktenzeichen",
        "Aktenzeichen",
        true,
        ""
      ),
      child: defineChipsField(
        "deviatingFileNumbers",
        "Abweichendes Aktenzeichen",
        "Abweichendes Aktenzeichen",
        false,
        ""
      ),
    }
  ),
  defineNestedInputField(
    "Toggle Abweichendes Entscheidungsdatum",
    "nestedInputOfDecisionDateAndDeviatingDecisionDate",
    {
      parent: defineDateField(
        "decisionDate",
        "Entscheidungsdatum",
        "Entscheidungsdatum",
        true,
        undefined
      ),
      child: defineChipsDateField(
        "deviationDecisionDate",
        "Abweichendes Entscheidungsdatum",
        "AbweichendesEntscheidungsdatum",
        true,
        undefined
      ),
    }
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
  defineNestedInputField(
    "Abweichender ECLI",
    "nestedInputOfEcliAndDeviatingEclis",
    {
      parent: defineTextField(
        "ecli",
        "ECLI",
        "ECLI",
        false,
        "",
        { defaultMessage: "", field: "" },
        false
      ),
      child: defineChipsField(
        "deviatingEclis",
        "Abweichender ECLI",
        "Abweichender ECLI",
        false,
        ""
      ),
    }
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
    undefined
  ),
]
