# TODO get this list from code directly
component "MailTrackingService" "Übersetzt Events aus der Rückmeldung des jDV Versands"
component "SendInBlueMailTrackingService"

component "EmailPublishService" "Veröffentlicht Dokumentationseinheiten per Email an die jDV"
component "XmlEMailPublishService" "Versendet Emails"

component "DocumentUnitStatusService" "Bearbeitet den Status von Dokumentationseinheiten"
component "DatabaseDocumentUnitStatusService" "Funktionalität rund um Dokumentationsstatus"

component "DocumentNumberService" "Stellt Dokumentennummern bereit"
component "DatabaseDocumentNumberService" "Funktionalität rund um Doknummern"

component "LookupTableService"
component "LookupTableImporterService" "Importiert Wertetabellen"

component "FeatureToggleService" "Stellt Feature Toggles zur Nutzung bereit"
component "UnleashService" "Holt Feature Toggle Informationen"

component "UserService" "Stellt Nutzerinfromationen bereit"
component "KeycloakUserService" "Holt Informationen zu Nutzern und Gruppen von Keycloak"

component "NormAbbreviationService" "Funktionalität rund um Normenabkürzung"
component "FieldOfLawService" "Funktionalität rund um Rechtsgebiete"
component "AuthService" "Autorisierung von Nutzerzugriffen"
component "KeywordService" "Funktionalität rund um Normenabkürzung"
component "DocxConverterService" "Konvertiert Rechtsprechungsdokumente im Dokumentationseinheiten"
component "DocumentUnitService" "Funktionalität rund um Dokumentationseinheiten"