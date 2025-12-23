import { Component } from "vue"
import { LabelPosition } from "@/components/input/InputField.vue"
import { CitationType } from "@/domain/citationType"
import { Court } from "@/domain/court"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { DocumentType } from "@/domain/documentType"

import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { LanguageCode } from "@/domain/foreignLanguageVersion"
import { LegalForceRegion, LegalForceType } from "@/domain/legalForce"
import LegalPeriodical from "@/domain/legalPeriodical"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import { Procedure } from "@/domain/procedure"
import { User } from "@/domain/user"
import { SelectablePanelContent } from "@/types/panelContentMode"
import { Match } from "@/types/textCheck"

export enum InputType {
  TEXT = "text",
  FILE = "file",
  DROPDOWN = "dropdown",
  DATE = "date",
  CHECKBOX = "checkbox",
  RADIO = "radio",
  CHIPS = "chips",
  DATECHIPS = "datechips",
  NESTED = "nested",
  COMBOBOX = "combobox",
  TEXTAREA = "textarea",
  DATE_TIME = "date_time",
  YEAR = "year",
  TIME = "time",
  UNDEFINED_DATE = "undefined_date",
}

//BASE
export interface BaseInputAttributes {
  ariaLabel: string
  validationError?: ValidationError
  labelPosition?: LabelPosition
}

export interface BaseInputField {
  name: string
  type: InputType
  label: string
  required?: boolean
  inputAttributes: BaseInputAttributes
}

//TEXT
export type TextInputModelType = string

export interface TextInputAttributes extends BaseInputAttributes {
  placeholder?: string
  readOnly?: boolean
  maxlength?: string
  autofocus?: boolean
}

export interface TextInputField extends BaseInputField {
  type: InputType.TEXT
  inputAttributes: TextInputAttributes
}

//CHIPS
export type ChipsInputModelType = string[]

export interface ChipsInputAttributes extends BaseInputAttributes {
  placeholder?: string
  readOnly?: boolean
}

export interface ChipsInputField extends BaseInputField {
  type: InputType.CHIPS
  inputAttributes: ChipsInputAttributes
}

export interface DateChipsInputField extends BaseInputField {
  type: InputType.DATECHIPS
  inputAttributes: ChipsInputAttributes
}

//NESTED INPUT
export interface NestedInputModelType {
  fields: {
    parent: ModelType
    child: ModelType
  }
}

//DATE
export interface DateAttributes extends BaseInputAttributes {
  isFutureDate?: boolean
}

export interface DateInputField extends BaseInputField {
  placeholder?: string
  type: InputType.DATE
  inputAttributes: DateAttributes
}

export type DateInputModelType = string | undefined

//DROPDOWN
export type DropdownInputModelType = string | undefined

export type DropdownItem = {
  label: string
  value: DropdownInputModelType
}

export interface DropdownAttributes extends BaseInputAttributes {
  placeholder?: string
  items: DropdownItem[]
}

export interface DropdownInputField extends BaseInputField {
  type: InputType.DROPDOWN
  inputAttributes: DropdownAttributes
}

//COMBOBOX
export type ComboboxInputModelType =
  | DocumentType
  | Court
  | NormAbbreviation
  | FieldOfLaw
  | CitationType
  | Procedure
  | LegalForceRegion
  | LegalForceType
  | LegalPeriodical
  | DocumentationOffice
  | LanguageCode
  | User

//CHECKBOX
export type BooleanModelType = boolean

export type CheckboxInputModelType = boolean

export interface CheckboxInputField extends BaseInputField {
  type: InputType.CHECKBOX
  inputAttributes: BaseInputAttributes
}

//TEXTAREA
export type TextaraInputModelType = string

export interface TextAreaInputAttributes extends BaseInputAttributes {
  placeholder?: string
  readOnly?: boolean
  autosize?: boolean
  rows?: number
  fieldSize: "max" | "big" | "medium" | "small"
}

export interface TextAreaInputField extends BaseInputField {
  type: InputType.TEXTAREA
  inputAttributes: TextAreaInputAttributes
}

export type ModelType =
  | TextInputModelType
  | DateInputModelType
  | DropdownInputModelType
  | BooleanModelType
  | CheckboxInputModelType
  | ChipsInputModelType
  | NestedInputModelType
  | ComboboxInputModelType
  | DocumentationOffice
  | TextaraInputModelType

export type ValidationError = {
  code?: string
  message: string
  instance: string
}

export type ExtraContentSidePanelProps = {
  documentUnit?: DocumentationUnit
  showEditButton?: boolean
  hidePanelModeBar?: boolean
  sidePanelMode?: SelectablePanelContent
  sidePanelShortcut?: string
  icon?: Component
  jumpToMatch?: (match: Match) => void
}

export type LdmlPreview = {
  ldml?: string
  success?: boolean
  statusMessages?: string[]
}

export type ComboboxItem<T> = {
  label: string
  value?: T
  additionalInformation?: string
  sideInformation?: string
}
