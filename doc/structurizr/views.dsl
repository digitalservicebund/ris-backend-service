views {
    # purpose: overview of the system with relations to external systems
    systemContext ris "SystemContext" {
        include *
        exclude user
        exclude openIdProvider
        exclude emailApiProvider
        exclude emailHoster
        exclude errorMonitoring
        exclude jurisSftp
    }

    # purpose: show containers used while development
    deployment ris "DevelopmentLocalEnv" {
        include *
    }

    # purpose: show containers used in the staging environment
    deployment ris "DevelopmentCloudEnv" {
        include *
    }

    # purpose: show containers used in the user acceptance testing environment
    #deployment ris "UATCloudEnv" {
    #    include *
    #}

    # purpose: show containers used in the production environment
    #deployment ris "ProductionCloudEnv" {
    #    include *
    #}

    container ris "UsersAndContainersRSP" {
        include *
        
        # specific users
        exclude user
        exclude normsDocumentary
        exclude publicUser

        # external systems
        exclude openIdProvider
        exclude emailApiProvider
        exclude emailHoster
        exclude errorMonitoring
        exclude jurisSftp

        # norms containers
        exclude normBackend
        exclude normFrontend
        exclude normCodeEditor

        # migration containers
        exclude normMigration

        # portal containers
        exclude portalFrontend
    }

    container ris "UsersAndContainersNORM" {
        include *

        # specific users
        exclude user
        exclude caselawDocumentary
        exclude publicUser

        # external systems
        exclude openIdProvider
        exclude emailApiProvider
        exclude emailHoster
        exclude errorMonitoring
        exclude jurisSftp

        # caselaw containers
        exclude rspBackend
        exclude rspFrontend
        exclude fileStore

        # migration containers
        exclude rspMigration

        # portal containers
        exclude portalFrontend

        # shared infrastructure containers
        exclude featureFlags
    }

    container ris "UsersAndContainersPortal" {
        include *

        # specific users
        exclude caselawDocumentary
        exclude normsDocumentary

        # external systems
        exclude openIdProvider
        exclude emailApiProvider
        exclude emailHoster
        exclude errorMonitoring
        exclude jurisSftp

        # caselaw containers
        exclude fileStore

        # norms containers
        exclude normBackend
        exclude normFrontend
        exclude normCodeEditor

        # migration containers
        exclude rspMigration
        exclude normMigration
        exclude sftpImport
        exclude migrationStore

        # shared infrastructure containers
        exclude featureFlags
        exclude sessionStore
        exclude database
    }

    container ris "Migration" {
        include sftpImport
        include rspMigration
        include normMigration
        include jurisSftp
        include database
        include rspBackend
        include normBackend
        include migrationStore
    }

    container ris "Monitoring" {
        include rspBackend
        include rspFrontend
        include rspMigration
        include monitoring
        include errorMonitoring
        
        include systemAdministrator
    }

    container ris "IdentityAndAccessManagement" {
        include rspBackend
        include rspFrontend
        
        include openIdProvider
        
        include caselawDocumentary
        include normsDocumentary
        include systemAdministrator
    }

    container ris "ContainerAndExternalsRSP" {
        include rspBackend
        include rspFrontend
        
        include openIdProvider
        include emailApiProvider
        include emailHoster
        include errorMonitoring

        include user
    }

    container ris "DevelopedByDigitalService" {
        include rspBackend
        include rspFrontend
        
        include portalBackend
        include portalFrontend
        include rspMigration
    }

    systemlandscape "SystemLandscape" {
        include *
        exclude caselawDocumentary
        exclude normsDocumentary
        exclude systemAdministrator
        exclude publicUser
    }

    component rspBackend "BackendComponentsCaselaw" {
        include *
    }

    component normBackend "BackendComponentsNorms" {
        include *
    }

    styles {
        element "Person" {
            color #ffffff
            background #08427B
            fontSize 22
            shape Person
        }
        element "Software System" {
            color #ffffff
            background #1168BD
        }
        element "Container" {
            color #ffffff
            background #438DD5
        }
        element "datastore" {
            shape Cylinder
        }
        element "saas" {
            background grey
        }
        element "portal" {
            background #DB005B
        }
        element "migration" {
            background #02a35a
        }

    }
}
