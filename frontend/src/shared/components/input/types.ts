import { LabelPosition } from "@/shared/components/input/InputField.vue"

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
  CUSTOMDATE = "customdate",
  NEWDATE = "newdate",
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
  label?: string
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

export interface NestedInputAttributes extends BaseInputAttributes {
  fields: { parent: InputField; child: InputField }
}

export interface NestedInputField extends Omit<BaseInputField, "name"> {
  name: `nestedInputOf${Capitalize<string>}And${Capitalize<string>}`
  type: InputType.NESTED
  inputAttributes: NestedInputAttributes
}

//DATE
export interface DateAttributes extends BaseInputAttributes {
  isFutureDate?: boolean
}

export interface DateInputField extends BaseInputField {
  placeholder?: string
  type: InputType.NEWDATE
  inputAttributes: DateAttributes
}

export type DateInputModelType = string | undefined

//DROPDOWN
export type DropdownInputModelType = string

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
export type ComboboxInputModelType = string | { label: string }

export type ComboboxItem = {
  label: string
  value: ComboboxInputModelType
}

export interface ComboboxAttributes extends BaseInputAttributes {
  itemService: (filter?: string) => Promise<{
    status: number
    data?: ComboboxItem[]
    error?: {
      title: string
      description?: string
      validationErrors?: ValidationError[]
    }
  }>
  placeholder?: string
}

export interface ComboboxInputField extends BaseInputField {
  type: InputType.COMBOBOX
  inputAttributes: ComboboxAttributes
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
  | DateChipsInputField
  | NestedInputField
  | ComboboxInputField

export type InputAttributes =
  | TextInputAttributes
  | DropdownAttributes
  | ChipsInputAttributes
  | NestedInputAttributes
  | DateAttributes
  | ComboboxAttributes

export type ModelType =
  | TextInputModelType
  | DateInputModelType
  | DropdownInputModelType
  | BooleanModelType
  | CheckboxInputModelType
  | ChipsInputModelType
  | NestedInputModelType
  | ComboboxInputModelType

// TODO We keep the name `defaultMessage` from the backend response,
// but this could ne misleading. We should rename this to `message`.
export type ValidationError = {
  defaultMessage: string
  field: string
}
