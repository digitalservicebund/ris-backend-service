export enum InputType {
  TEXT = "text",
  FILE = "file",
  DROPDOWN = "dropdown",
  DATE = "date",
}

export interface BaseInputAttributes {
  ariaLabel: string
  validationError?: ValidationError
}

export interface BaseInputField {
  name: string
  type: InputType
  label: string
  required?: boolean
  inputAttributes: BaseInputAttributes
}

export type TextInputModelType = string

export interface TextInputAttributes extends BaseInputAttributes {
  placeholder?: string
  readOnly?: boolean
}

export interface TextInputField extends BaseInputField {
  type: InputType.TEXT
  inputAttributes: TextInputAttributes
}

export type DateInputModelType = string

export interface DateInputField extends BaseInputField {
  type: InputType.DATE
  inputAttributes: BaseInputAttributes
}

export enum LookupTableEndpoint {
  documentTypes = "lookuptable/documentTypes",
}

export type DropdownItem = {
  text: string
  value: string
}
export interface DropdownAttributes extends BaseInputAttributes {
  isCombobox?: boolean
  placeholder?: string
  dropdownItems?: DropdownItem[] | LookupTableEndpoint
  preselectedValue?: string
}

export interface DropdownInputField extends BaseInputField {
  type: InputType.DROPDOWN
  inputAttributes: DropdownAttributes
}

export type InputField = TextInputField | DropdownInputField | DateInputField
export type InputAttributes = TextInputAttributes | DropdownAttributes
export type ModelType = TextInputModelType | DateInputModelType

export type ValidationError = {
  defaultMessage: string
  field: string
}
