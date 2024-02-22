views {
    systemContext ris "SystemContext" {
        include *
        exclude user
        exclude openIdProvider
        exclude emailApiProvider
        exclude emailHoster
        exclude errorMonitoring
        exclude jurisSftp
    }

    deployment ris "DevelopmentLocal" {
        include *
    }

    deployment ris "DevelopmentCloud" {
        include *
    }

    container ris "UsersAndContainers" {
        include *
        exclude caselawDocumentary
        exclude normsDocumentary
        exclude openIdProvider
        exclude emailApiProvider
        exclude emailHoster
        exclude errorMonitoring
        exclude jurisSftp
    }

    container ris "Migration" {
        include risMigration
        include jurisSftp
        include database
        include risBackend
        include fileStore
    }

    container ris "Monitoring" {
        include risBackend
        include risFrontend
        include risMigration
        include monitoring
        include errorMonitoring
        
        include systemAdministrator
    }

    container ris "IdentityAndAccessManagement" {
        include risBackend
        include risFrontend
        
        include openIdProvider
        
        include caselawDocumentary
        include normsDocumentary
        include systemAdministrator
    }

    container ris "ContainerAndExternals" {
        include risBackend
        include risFrontend
        
        include openIdProvider
        include emailApiProvider
        include emailHoster
        include errorMonitoring

        include user
    }

    container ris "DevelopedByDigitalService" {
        include risBackend
        include risFrontend
        
        include portalBackend
        include portalFrontend
        include risMigration
    }

    systemlandscape "SystemLandscape" {
        include *
        exclude caselawDocumentary
        exclude normsDocumentary
        exclude systemAdministrator
        exclude publicUser
    }

    component risBackend "BackendComponents" {
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
