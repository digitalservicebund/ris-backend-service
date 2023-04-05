import {
  InputType,
  NestedInputAttributes,
  NestedInputField,
  ValidationError,
  InputField,
  DropdownItem,
  ComboboxAttributes,
} from "../../shared/components/input/types"
import legalEffectTypes from "@/data/legalEffectTypes.json"
import DocumentUnit from "@/domain/documentUnit"
import comboboxItemService from "@/services/comboboxItemService"

export function defineTextField(
  name: string,
  id: string,
  label: string,
  ariaLabel: string,
  placeholder?: string,
  validationError?: ValidationError,
  readOnly?: boolean
): InputField {
  return {
    name,
    id,
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
  id: string,
  label: string,
  ariaLabel: string,
  placeholder?: string
): InputField {
  return {
    name,
    id,
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
  id: string,
  label: string,
  ariaLabel: string,
  placeholder?: string
): InputField {
  return {
    name,
    id,
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
  id: string,
  label: string,
  ariaLabel: string,
  validationError?: ValidationError
): InputField {
  return {
    name,
    id,
    type: InputType.DATE,
    label,
    required: DocumentUnit.isRequiredField(name),
    inputAttributes: { ariaLabel, validationError },
  }
}

export function defineComboboxField(
  name: string,
  id: string,
  label: string,
  ariaLabel: string,
  itemService: ComboboxAttributes["itemService"],
  placeholder?: string,
  validationError?: ValidationError
): InputField {
  return {
    name,
    id,
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
  id: string,
  label: string,
  ariaLabel: string,
  items: DropdownItem[],
  placeholder?: string,
  validationError?: ValidationError
): InputField {
  return {
    name,
    id,
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
  id: NestedInputField["name"],
  fields: NestedInputAttributes["fields"]
): InputField {
  return {
    name,
    id,
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
    "nestedInputOfCourtAndIncorrectCourts",
    {
      parent: defineComboboxField(
        "court",
        "court",
        "Gericht",
        "Gericht",
        comboboxItemService.getCourts,
        "Gerichtstyp Gerichtsort"
      ),
      child: defineChipsField(
        "incorrectCourts",
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
    "nestedInputOfFileNumbersAndDeviatingFileNumbers",
    {
      parent: defineChipsField(
        "fileNumbers",
        "fileNumbers",
        "Aktenzeichen",
        "Aktenzeichen",
        ""
      ),
      child: defineChipsField(
        "deviatingFileNumbers",
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
    "nestedInputOfDecisionDateAndDeviatingDecisionDates",
    {
      parent: defineDateField(
        "decisionDate",
        "decisionDate",
        "Entscheidungsdatum",
        "Entscheidungsdatum",
        undefined
      ),
      child: defineChipsDateField(
        "deviatingDecisionDates",
        "deviatingDecisionDates",
        "Abweichendes Entscheidungsdatum",
        "Abweichendes Entscheidungsdatum",
        undefined
      ),
    }
  ),
  defineTextField(
    "appraisalBody",
    "appraisalBody",
    "Spruchkörper",
    "Spruchkörper"
  ),
  defineComboboxField(
    "documentType",
    "documentType",
    "Dokumenttyp",
    "Dokumenttyp",
    comboboxItemService.getDocumentTypes,
    "Bitte auswählen"
  ),
  defineNestedInputField(
    "Abweichender ECLI",
    "nestedInputOfEcliAndDeviatingEclis",
    "nestedInputOfEcliAndDeviatingEclis",
    {
      parent: defineTextField("ecli", "ecli", "ECLI", "ECLI", ""),
      child: defineChipsField(
        "deviatingEclis",
        "deviatingEclis",
        "Abweichender ECLI",
        "Abweichender ECLI",
        ""
      ),
    }
  ),
  defineTextField("procedure", "procedure", "Vorgang", "Vorgang"),
  defineDropdownField(
    "legalEffect",
    "legalEffect",
    "Rechtskraft",
    "Rechtskraft",
    legalEffectTypes.items
  ),
  defineTextField(
    "region",
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
