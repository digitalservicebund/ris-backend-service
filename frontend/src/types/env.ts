export type Environment = "staging" | "uat" | "production"

export type Env = { environment: Environment; portalUrl?: string }
