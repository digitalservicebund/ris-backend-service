import { Court } from "@/domain/documentUnit"

export enum InputType {
  TEXT = "text",
  FILE = "file",
  DROPDOWN = "dropdown",
  DATE = "date",
  CHECKBOX = "checkbox",
  MULTITEXT = "multitext",
  TUPLE = "tuple",
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

//MULTITEXT
export type MultitextInputModelType = string[]

export interface MultitextInputAttributes extends BaseInputAttributes {
  placeholder?: string
}

export interface MultitextInputField extends BaseInputField {
  type: InputType.MULTITEXT
  inputAttributes: MultitextInputAttributes
}

//TUPLE
export interface TupleInputModelType {
  fields: {
    parent: ModelType
    child: ModelType
  }
}

export interface TupleInputAttributes extends BaseInputAttributes {
  fields: { parent: InputField; child: InputField }
}

export interface TupleInputField extends BaseInputField {
  type: InputType.TUPLE
  inputAttributes: TupleInputAttributes
}

//DATE
export interface DateInputField extends BaseInputField {
  type: InputType.DATE
  inputAttributes: BaseInputAttributes
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

export interface DropdownAttributes extends BaseInputAttributes {
  isCombobox?: boolean
  placeholder?: string
  dropdownItems?: DropdownItem[]
  endpoint?: LookupTableEndpoint
  preselectedValue?: string
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
  | MultitextInputField
  | TupleInputField

export type InputAttributes =
  | TextInputAttributes
  | DropdownAttributes
  | MultitextInputAttributes
  | TupleInputAttributes

export type ModelType =
  | TextInputModelType
  | DateInputModelType
  | DropdownInputModelType
  | BooleanModelType
  | CheckboxInputModelType
  | MultitextInputModelType
  | TupleInputModelType

export type ValidationError = {
  defaultMessage: string
  field: string
}
