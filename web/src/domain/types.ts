import { Court } from "@/domain/documentUnit"

export enum InputType {
  TEXT = "text",
  FILE = "file",
  DROPDOWN = "dropdown",
  DATE = "date",
  CHECKBOX = "checkbox",
  CHIPS = "chips",
  NESTED = "nested",
}

//BASE
export interface BaseInputAttributes {
  ariaLabel: string
  validationError?: ValidationError
}

export interface BaseInputField {
  name: string
  type: InputType
  label?: string
  required?: boolean
  inputAttributes: BaseInputAttributes
}

//TEXT
export type TextInputModelType = string

export interface TextInputAttributes extends BaseInputAttributes {
  placeholder?: string
  readOnly?: boolean
}

export interface TextInputField extends BaseInputField {
  type: InputType.TEXT
  inputAttributes: TextInputAttributes
}

//CHIPS
export type ChipsInputModelType = string[]

export interface ChipsInputAttributes extends BaseInputAttributes {
  placeholder?: string
}

export interface ChipsInputField extends BaseInputField {
  type: InputType.CHIPS
  inputAttributes: ChipsInputAttributes
}

//NESTED INPUT
export interface NestedInputModelType {
  fields: {
    parent: ModelType
    child: ModelType
  }
}

export interface NestedInputAttributes extends BaseInputAttributes {
  fields: { parent: InputField; child: InputField }
}

export interface NestedInputField extends BaseInputField {
  type: InputType.NESTED
  inputAttributes: NestedInputAttributes
}

//DATE
export interface DateInputField extends BaseInputField {
  type: InputType.DATE
  inputAttributes: DateAttributes
}

export type DateInputModelType = string

//LOOKUP
export enum LookupTableEndpoint {
  documentTypes = "lookuptable/documentTypes",
  courts = "lookuptable/courts",
}

//DROPDOWN
export type DropdownInputModelType = string | Court

export type DropdownItem = {
  text: string
  value: DropdownInputModelType
}

export interface DateAttributes extends BaseInputAttributes {
  isFutureDate?: boolean
}

export interface DropdownAttributes extends BaseInputAttributes {
  isCombobox?: boolean
  placeholder?: string
  dropdownItems?: DropdownItem[]
  endpoint?: LookupTableEndpoint
}

export interface DropdownInputField extends BaseInputField {
  type: InputType.DROPDOWN
  inputAttributes: DropdownAttributes
}

//CHECKBOX
export type BooleanModelType = boolean

export type CheckboxInputModelType = boolean

export interface CheckboxInputField extends BaseInputField {
  type: InputType.CHECKBOX
  inputAttributes: BaseInputAttributes
}

export type InputField =
  | TextInputField
  | DropdownInputField
  | DateInputField
  | CheckboxInputField
  | ChipsInputField
  | NestedInputField

export type InputAttributes =
  | TextInputAttributes
  | DropdownAttributes
  | ChipsInputAttributes
  | NestedInputAttributes
  | DateAttributes

export type ModelType =
  | TextInputModelType
  | DateInputModelType
  | DropdownInputModelType
  | BooleanModelType
  | CheckboxInputModelType
  | ChipsInputModelType
  | NestedInputModelType

export type ValidationError = {
  defaultMessage: string
  field: string
}
