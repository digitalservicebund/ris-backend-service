# the architecture model
model {
    caselawDocumentary = person "Caselaw Documentary" 
    normsDocumentary = person "Norms Documentary"
    systemAdministrator = person "System Administrator"
    user = person "User"
    publicUser = person "Public User" "A public human user" "portal"

    group "DigitalService" {
        ris = softwareSystem "RIS" {

            # Caselaw
            rspFrontend = container "RSP Web-Anwendung" "Stellt die grafische Nutzerschnittstelle für Rechtsprechung zur Verfügung" "Typescript, Vue"
            rspBackend = container "RSP API Anwendung" "Bietet sämtliche backend Funktionalitäten zum Dokumentieren von Rechtsprechung an" "Java, Spring, WebFlux" {
                !include ./rspComponents.dsl
            }
            fileStore = container "Datei Speicher" "Speichert alle Dokumente und Tabellen" "S3 compatible" "datastore"

            # Norms
            normFrontend = container "NORM Web-Anwendung" "Stellt das grafische Nutzerschnittstelle für Normen zur Verfügung" "Typescript, Vue"
            # Open question: will normCodeEditor be a component in frontend or a container on its own?
            normCodeEditor = container "NORM XML-Editor" "Stellt das Nutzerschnittstelle und die Logik zum Beabreiten von LegaldocML XML Quellcode" "code-server"
            normBackend = container "NORM API Anwendung" "Bietet sämtliche backend Funktionalitäten zum Dokumentieren von Normen an" "Java, Spring, WebFlux" {
                !include ./normComponents.dsl
            }

            # Search / Portal
            portalFrontend = container "Portal Web-Anwendung" "Grafisches Nutzerschnittstelle zur Recherche" "Typescript, Vue (?)" "portal"
            portalBackend = container "Portal API-Anwendung" "Schnittstelle zur Suchfunktion und Speicher von Suchen " "Java" "portal"
            searchEngine = container "Suchindex" "Speichert und indiziert Dokumente" "OpenSearch" "datastore,portal"

            # Migration
            sftpImport = container "Migration Data Import" "Kommandozeilenwerkzeug zur Übernahme der jDV Daten vom juris SFTP Server" "rclone" "migration"
            rspMigration = container "RSP Migration Tool" "Kommandozeilenwerkzeug zur Übernahme der jDV Daten nach Rechtsprechung" "Java" "migration"
            normMigration = container "NORM Migration Tool" "Kommandozeilenwerkzeug zur Übernahme der jDV Daten nach Normen" "Kotlin" "migration"
            migrationStore = container "Migration Quellen Speicher" "Ablage der von juris gelieferten Migrationsdaten" "S3 compatible" "datastore"


            # Shared infrastructure
            sessionStore = container "Nutzersession Speicher" "Speicher Nutzersession zwischen" "Redis" "datastore"
            database = container "Datenbank" "Speichert alle Dokumente und Tabellen" "PostgreSQL" "datastore"
            monitoring = container "Monitoring" "Stellt Systeminformationen grafisch da" "Grafana"
            featureFlags = container "Feature Flags" "An- und Abschalten besimmter Features in Abhängigkeit der Deployment Umgebung" "Unleash"
            
            publicationStore = container "Publikationsspeicher (?)" "Speichert die öffentlich verfügbaren Dokumente für das Portal" "S3(?), LegalDocML" "datastore,portal"

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

    rspBackend -> openIdProvider "prüft Nutzer" "HTTPS"
    rspBackend -> emailApiProvider "sendet E-Mails" "XML, HTTPS"
    rspBackend -> emailHoster "empfängt E-Mails" "XML, IMAP"
    rspBackend -> errorMonitoring "sendet Fehler" "JSON, HTTPS"
    
    rspFrontend -> errorMonitoring "sendet Fehler" "JSON, HTTPS" 

    normBackend -> openIdProvider "prüft Nutzer" "HTTPS"
    normBackend -> errorMonitoring "sendet Fehler" "JSON, HTTPS"
    
    normFrontend -> errorMonitoring "sendet Fehler" "JSON, HTTPS" 
    normCodeEditor -> errorMonitoring "sendet Fehler" "JSON, HTTPS" 

    sftpImport -> jurisSftp "spiegelt Tages- und Monatsexporte" "ZIP, SFTP"

    # relationships between people and containers
    caselawDocumentary -> rspFrontend "nutzt" "HTTPS"
    normsDocumentary -> normFrontend "nutzt" "HTTPS"
    normsDocumentary -> normCodeEditor "nutzt" "HTTPS"

    normsDocumentary -> rspFrontend "nutzt" "HTTPS"
    caselawDocumentary -> rspFrontend "nutzt" "HTTPS"
    systemAdministrator -> monitoring "liest Systeminformationen" "HTTPS"
    systemAdministrator -> featureFlags "konfiguriert Einstellungen" "HTTPS"

    # relationships between containers - caselaw
    rspFrontend -> rspBackend "nutzt" "JSON/HTTPS"
    
    rspBackend -> database "Speichern und Lesen" "JPA"
    rspBackend -> sessionStore "Speichern und Lesen" "RESP"
    rspBackend -> fileStore "Speichern un Lesen" "S3 Protocol"
    rspBackend -> publicationStore "publiziert nach" "LegalDocML, HTTPS"
    rspBackend -> portalBackend "sucht per" "JSON, HTTPS"
    rspBackend -> featureFlags "lädt Einstellungen" "JSON, HTTPS"

    # relationships between containers - norms
    normFrontend -> normBackend "nutzt" "JSON/HTTPS"
    normFrontend -> normCodeEditor "öffnet LegaldocML in" "HTTPS"
    normCodeEditor -> normBackend "lädt und speichert LegaldocML XML in" "HTTPS, JSON, XML"
    normCodeEditor -> normFrontend "meldet gespeicherte Aktualisierungen an" "HTTPS"

    normBackend -> portalBackend "sucht per" "JSON, HTTPS"
    normBackend -> database "Speichern und lesen" "JPA"

    # relationships between containers - migration
    sftpImport -> migrationStore "spiegelt Tages- und Monatsexporte" "ZIP, SFTP"

    rspMigration -> database "speichert migrierte Rechtsprechung" "JPA"
    rspMigration -> migrationStore "entpackt Tages- und Monatsexporte" "S3 Protocol"
    rspMigration -> monitoring "sendet Logs" "HTTPS"

    normMigration -> database "speichert migrierte Normen" "JPA"
    normMigration -> migrationStore "entpackt Tages- und Monatsexporte" "S3 Protocol"
    normMigration -> monitoring "sendet Logs" "HTTPS"

    # relationships between containers - search / portal
    portalFrontend -> portalBackend "nutzt" "HTTPS"
    portalFrontend -> publicationStore "verweist auf" "HTTPS"
    portalBackend -> publicationStore "liest ein" "HTTPS(?)"
    portalBackend -> searchEngine "speichert und indiziert Dok.einheiten" "JSON, HTTPS"

    # relationships between containers - shared infrastructure
    monitoring -> rspBackend "holt Systeminformationen" "HTTPS"
    monitoring -> normBackend "holt Systeminformationen" "HTTPS"

    # relationships to/from components # TODO generate in components.dsl from code

    !include ./deploymentEnvironments.dsl

}
