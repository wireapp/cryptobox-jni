pipeline {
    agent {
        label 'android-reloaded-builder'
    }

    options {
        parallelsAlwaysFailFast()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'version', defaultValue: '1.1.4')
        booleanParam(name: 'deploy', defaultValue: false)
    }

    stages {

        stage('Build') {
            agent {
                dockerfile true
            }
            steps {
                checkout scm
                writeFile file: 'mk/version.mk', text: "VERSION := $version"
                sh "rm -rf android/dist"
                sh "cd android && make dist"
                stash includes: 'android/dist/*', name: 'artifacts'
                archiveArtifacts artifacts: 'android/dist/*', followSymlinks: false
            }
        }

        stage('Upload to sonatype') {
            when {
                expression { return params.deploy }
            }
            steps {
                withCredentials([ usernamePassword( credentialsId: 'android-sonatype-nexus', usernameVariable: 'SONATYPE_USERNAME', passwordVariable: 'SONATYPE_PASSWORD' ),
                    file(credentialsId: 'D599C1AA126762B1.asc', variable: 'PGP_PRIVATE_KEY_FILE'),
                    string(credentialsId: 'PGP_PASSPHRASE', variable: 'PGP_PASSPHRASE') ]) {
                    withMaven(maven: 'M3') {
                        unstash 'artifacts'
                        sh(
                            script: """
                                touch local.properties
                                version=$version ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
                            """
                        )
                    }
                }
            }
        }
    }
}
