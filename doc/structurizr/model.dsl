# the architecture model
model {
    caselawDocumentary = person "Caselaw Documentary" 
    normsDocumentary = person "Norms Documentary"
    systemAdministrator = person "System Administrator"
    user = person "User"
    publicUser = person "Public User" "A public human user" "portal"

    group "DigitalService" {
        ris = softwareSystem "RIS" {
            risFrontend = container "Web-Anwendung" "Stellt das grafische Nutzerinterface zur Verfügung" "Typescript, Vue"
            risBackend = container "API Anwendung" "Bietet sämtliche backend Funktionalitäten zum Dokumentieren an" "Java, Kotlin, Spring, WebFlux" {
                !include ./components.dsl
            }
            sessionStore = container "Nutzersession Speicher" "Speicher Nutzersession zwischen" "Redis" "datastore"
            database = container "Datenbank" "Speichert alle Dokumente und Tabellen" "Postgresql" "datastore"
            fileStore = container "Datei Speicher" "Speichert alle Dokumente und Tabellen" "S3 compatible" "datastore"

            monitoring = container "Monitoring" "Stellt Systeminformationen grafisch da" "Grafana"

            publicationStore = container "Publikationsspeicher" "Speichert die öffentlich verfügbaren Dokumente für das Portal" "S3(?), LegalDocML" "datastore,portal"
            portalBackend = container "Portal API-Anwendung" "Indiziert und stellt Suchfunktion" "Java" "portal"
            portalFrontend = container "Portal Web-Anwendung" "Grafisches Nutzerinterface zur Recherche" "Typescript, Vue (?)" "portal"

            risMigration = container "Migration Tool" "Kommandozeilenwerkzeug zur Übernahme der jDV Daten" "Java" "migration"
        }
    }
    
    group "juris" {
        jurisDocumentationManagement = softwareSystem "jDV"
        jurisSftp = softwareSystem "juris SFTP"
    }
    
    group "Daten Konsumenten" {
        eLegislation = softwareSystem "E-Gesetzgebung"
    }

    group "Externe Anbieter" {
        openIdProvider = softwareSystem "IAM Anbieter" "Bare.ID (OpenID Connect)" "saas"
        
        emailApiProvider = softwareSystem "E-Mail Anbieter (Versand)" "SendInBlue" "saas"
        emailHoster = softwareSystem "E-Mail Anbieter (Empfang)" "IONOS" "saas"

        errorMonitoring = softwareSystem "Error Tracker" "Sentry überwacht und sammelt auftretende Fehler" "saas"
    }

    # relationships between people and software systems
    caselawDocumentary -> ris "dokumentiert Rechtsprechung"
    normsDocumentary -> ris "dokumentiert Normen"
    normsDocumentary -> jurisDocumentationManagement "lädt Normen"

    user -> openIdProvider "authentifiziert sich" "HTTPS"
    normsDocumentary -> openIdProvider "authentifiziert sich" "HTTPS"
    caselawDocumentary -> openIdProvider "authentifiziert sich" "HTTPS"
    systemAdministrator -> openIdProvider "verwaltet Nutzer" "HTTPS"

    publicUser -> portalFrontend "nutzt" "HTTPS"

    # relationships between systems
    ris -> jurisDocumentationManagement "sendet Dokumentationseinheiten"
    eLegislation -> ris "recherchiert Normen"

    # relationships between systems and containers
    jurisDocumentationManagement -> emailHoster "sendet Status-Mail zur Verarbeitung"

    risBackend -> openIdProvider "prüft Nutzer" "HTTPS"
    risBackend -> emailApiProvider "sendet E-Mails" "XML, HTTPS"
    risBackend -> emailHoster "empfängt E-Mails" "XML, IMAP"
    risBackend -> errorMonitoring "sendet Fehler" "JSON, HTTPS"
    
    risFrontend -> errorMonitoring "sendet Fehler" "JSON, HTTPS" 

    risMigration -> jurisSftp "spiegelt Tages- und Monatsexporte" "ZIP, SFTP"

    # relationships between people and containers
    user -> risFrontend "nutzt" "HTTPS"
    normsDocumentary -> risFrontend "nutzt" "HTTPS"
    caselawDocumentary -> risFrontend "nutzt" "HTTPS"
    systemAdministrator -> monitoring "liest Systeminformationen" "HTTPS"

    # relationships between containers
    risFrontend -> risBackend "nutzt" "JSON/HTTPS"
    
    risBackend -> database "speichern & lesen" "R2DBC"
    risBackend -> sessionStore "speichern & lesen" "RESP"
    risBackend -> fileStore "speichern & lesen" "S3 Protocol"
    risBackend -> publicationStore "publiziert nach" "LegalDocML, HTTPS"
    
    monitoring -> risBackend "holt Systeminformationen" "HTTPS"
    
    portalFrontend -> portalBackend "nutzt" "HTTPS"
    portalFrontend -> publicationStore "verweist auf" "HTTPS"
    portalBackend -> publicationStore "indiziert" "HTTPS(?)"

    risMigration -> database "speichert migrierte Daten" "JPA"
    risMigration -> risBackend "sendet Normen" "JSON, HTTPS"
    risMigration -> fileStore "speichert Tages- und Monatsexporte" "S3 Protocol"
    risMigration -> monitoring "sendet Logs" "HTTPS"

    # relationships to/from components # TODO generate in components.dsl from code

    !include ./deploymentEnvironments.dsl

}
