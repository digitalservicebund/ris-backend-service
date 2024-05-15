/**
 * Class representing the legal force (Gesetzeskraft) of a norm with required fields and validation methods.
 */
export default class LegalForce {
  /**
   * The unique identifier for the legal force.
   * @type {string | undefined}
   */
  uuid?: string

  /**
   * The type of legal force.
   * @type {LegalForceType | undefined}
   * @description Typ der Ges.-Kraft
   */
  type?: LegalForceType

  /**
   * The region associated with the legal force.
   * @type {LegalForceRegion | undefined}
   * @description Geltungsbereich
   */
  region?: LegalForceRegion

  /**
   * List of required fields for the legal force.
   */
  static readonly requiredFields = ["type", "region"] as const

  /**
   * List of all fields for the legal force.
   */
  static readonly fields = ["type", "region"] as const

  /**
   * Creates an instance of LegalForce.
   * @param {Partial<LegalForce>} [data={}] - Partial data to initialize the LegalForce instance.
   */
  constructor(data: Partial<LegalForce> = {}) {
    Object.assign(this, data)
  }

  /**
   * Checks if a field is empty.
   * @private
   * @param {LegalForce[(typeof LegalForce.fields)[number]]} value - The value of the field to check.
   * @returns {boolean} `true` if the field is empty; otherwise, `false`.
   */
  private fieldIsEmpty(
    value: LegalForce[(typeof LegalForce.fields)[number]],
  ): boolean {
    return value === undefined || !value || Object.keys(value).length === 0
  }

  /**
   * Indicates whether there are any required fields missing.
   * @type {boolean}
   * @readonly
   */
  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  /**
   * Gets the list of missing required fields.
   * @type {string[]}
   * @readonly
   */
  get missingRequiredFields() {
    return LegalForce.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }
}

/**
 * Type representing the legal force type.
 * @type LegalForceType
 * @property {string} [uuid] - The unique identifier for the legal force type.
 * @property {string} abbreviation - The abbreviation of the legal force type.
 */
export type LegalForceType = {
  uuid?: string
  abbreviation: string
}

/**
 * Type representing the legal force region.
 * @type LegalForceRegion
 * @property {string} [uuid] - The unique identifier for the legal force region.
 * @property {string} [code] - The code of the legal force region.
 * @property {string} longText - The long text description of the legal force region.
 */
export type LegalForceRegion = {
  uuid?: string
  code?: string
  longText: string
}
