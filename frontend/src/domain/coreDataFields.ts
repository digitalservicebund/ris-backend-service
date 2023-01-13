import {
  InputType,
  NestedInputAttributes,
  NestedInputField,
  ValidationError,
  InputField,
  DropdownItem,
  ComboboxAttributes,
} from "./types"
import legalEffectTypes from "@/data/legalEffectTypes.json"
import DocumentUnit from "@/domain/documentUnit"
import comboboxItemService from "@/services/comboboxItemService"

export function defineTextField(
  name: string,
  label: string,
  ariaLabel: string,
  placeholder?: string,
  validationError?: ValidationError,
  readOnly?: boolean
): InputField {
  return {
    name,
    type: InputType.TEXT,
    label,
    required: DocumentUnit.isRequiredField(name),
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
  placeholder?: string
): InputField {
  return {
    name,
    type: InputType.CHIPS,
    label,
    required: DocumentUnit.isRequiredField(name),
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
  placeholder?: string
): InputField {
  return {
    name,
    type: InputType.DATECHIPS,
    label,
    required: DocumentUnit.isRequiredField(name),
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
  validationError?: ValidationError
): InputField {
  return {
    name,
    type: InputType.DATE,
    label,
    required: DocumentUnit.isRequiredField(name),
    inputAttributes: { ariaLabel, validationError },
  }
}

export function defineComboboxField(
  name: string,
  label: string,
  ariaLabel: string,
  itemService: ComboboxAttributes["itemService"],
  placeholder?: string,
  validationError?: ValidationError
): InputField {
  return {
    name,
    type: InputType.COMBOBOX,
    label,
    required: DocumentUnit.isRequiredField(name),
    inputAttributes: {
      ariaLabel,
      placeholder,
      itemService,
      validationError,
    },
  }
}

export function defineDropdownField(
  name: string,
  label: string,
  ariaLabel: string,
  items: DropdownItem[],
  placeholder?: string,
  validationError?: ValidationError
): InputField {
  return {
    name,
    type: InputType.DROPDOWN,
    label,
    required: DocumentUnit.isRequiredField(name),
    inputAttributes: {
      ariaLabel,
      placeholder,
      items,
      validationError,
    },
  }
}

export function defineNestedInputField(
  ariaLabel: string,
  name: NestedInputField["name"],
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
  defineNestedInputField(
    "Fehlerhaftes Gericht",
    "nestedInputOfCourtAndIncorrectCourts",
    {
      parent: defineComboboxField(
        "court",
        "Gericht",
        "Gericht",
        comboboxItemService.getCourts,
        "Gerichtstyp Gerichtsort"
      ),
      child: defineChipsField(
        "incorrectCourts",
        "Fehlerhaftes Gericht",
        "Fehlerhaftes Gericht",
        ""
      ),
    }
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
        ""
      ),
      child: defineChipsField(
        "deviatingFileNumbers",
        "Abweichendes Aktenzeichen",
        "Abweichendes Aktenzeichen",
        ""
      ),
    }
  ),
  defineNestedInputField(
    "Abweichendes Entscheidungsdatum",
    "nestedInputOfDecisionDateAndDeviatingDecisionDates",
    {
      parent: defineDateField(
        "decisionDate",
        "Entscheidungsdatum",
        "Entscheidungsdatum",
        undefined
      ),
      child: defineChipsDateField(
        "deviatingDecisionDates",
        "Abweichendes Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        undefined
      ),
    }
  ),
  defineTextField("appraisalBody", "Spruchkörper", "Spruchkörper"),
  defineComboboxField(
    "documentType",
    "Dokumenttyp",
    "Dokumenttyp",
    comboboxItemService.getDocumentTypes,
    "Bitte auswählen"
  ),
  defineNestedInputField(
    "Abweichender ECLI",
    "nestedInputOfEcliAndDeviatingEclis",
    {
      parent: defineTextField("ecli", "ECLI", "ECLI", "", {
        defaultMessage: "",
        field: "",
      }),
      child: defineChipsField(
        "deviatingEclis",
        "Abweichender ECLI",
        "Abweichender ECLI",
        ""
      ),
    }
  ),
  defineTextField("procedure", "Vorgang", "Vorgang"),
  defineDropdownField(
    "legalEffect",
    "Rechtskraft",
    "Rechtskraft",
    legalEffectTypes.items
  ),
  defineTextField(
    "region",
    "Region",
    "Region",
    "",
    { defaultMessage: "", field: "" },
    true
  ),
]

export const fieldLabels: { [name: string]: string } = Object.assign(
  {},
  ...[...coreDataFields, ...courtFields]
    .reduce((flatArray, field) => {
      if (field.type === InputType.NESTED) {
        flatArray.push(field.inputAttributes.fields.parent)
        flatArray.push(field.inputAttributes.fields.child)
      } else flatArray.push(field)

      return flatArray
    }, [] as InputField[])
    .map((field) => ({ [field.name]: field.label as string }))
)
