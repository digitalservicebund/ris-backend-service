import { Court } from "@/domain/documentUnit"

export enum InputType {
  TEXT = "text",
  FILE = "file",
  DROPDOWN = "dropdown",
  DATE = "date",
  CHECKBOX = "checkbox",
  CHIP = "chip",
}

export interface BaseInputAttributes {
  ariaLabel: string
  validationError?: ValidationError
  subField?: InputField
}

export interface BaseInputField {
  name: string
  type: InputType
  label: string
  required?: boolean
  inputAttributes: BaseInputAttributes
}

export type TextInputModelType = string

export type BooleanModelType = boolean

export interface TextInputAttributes extends BaseInputAttributes {
  placeholder?: string
  readOnly?: boolean
}

export interface ChipInputAttributes extends BaseInputAttributes {
  placeholder?: string
}

export interface TextInputField extends BaseInputField {
  type: InputType.TEXT
  inputAttributes: TextInputAttributes
}

export interface ChipInputField extends BaseInputField {
  type: InputType.CHIP
  inputAttributes: ChipInputAttributes
}

export type DateInputModelType = string

export type ChipInputModelType = string[]

export interface DateInputField extends BaseInputField {
  type: InputType.DATE
  inputAttributes: BaseInputAttributes
}

export enum LookupTableEndpoint {
  documentTypes = "lookuptable/documentTypes",
  courts = "lookuptable/courts",
}

export type DropdownInputModelType = string | Court

export type DropdownItem = {
  text: string
  value: DropdownInputModelType
}

export interface DropdownAttributes extends BaseInputAttributes {
  isCombobox?: boolean
  placeholder?: string
  dropdownItems?: DropdownItem[]
  endpoint?: LookupTableEndpoint
  preselectedValue?: string
}

export type CheckboxInputModelType = boolean

export interface CheckboxInputField extends BaseInputField {
  type: InputType.CHECKBOX
  inputAttributes: BaseInputAttributes
}

export interface DropdownInputField extends BaseInputField {
  type: InputType.DROPDOWN
  inputAttributes: DropdownAttributes
}

export type InputField =
  | TextInputField
  | DropdownInputField
  | DateInputField
  | CheckboxInputField
  | ChipInputField

export type InputAttributes =
  | TextInputAttributes
  | DropdownAttributes
  | ChipInputAttributes

export type ModelType =
  | TextInputModelType
  | DateInputModelType
  | DropdownInputModelType
  | BooleanModelType
  | CheckboxInputModelType
  | ChipInputModelType

export type ValidationError = {
  defaultMessage: string
  field: string
}
