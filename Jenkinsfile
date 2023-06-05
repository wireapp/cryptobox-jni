pipeline {
    agent {
        label 'android-reloaded-builder'
    }

    options {
        parallelsAlwaysFailFast()
        disableConcurrentBuilds()
    }

    stages {

        stage('Build') {
            agent {
                dockerfile true
            }
            steps {
                checkout scm
                writeFile file: 'mk/version.mk', text: "VERSION := $version"
                sh "cd android && make dist"
                archiveArtifacts artifacts: 'android/dist/*', followSymlinks: false
            }
        }

        stage('Upload to sonatype') {
            steps {
                withCredentials([ usernamePassword( credentialsId: 'android-sonatype-nexus', usernameVariable: 'SONATYPE_USERNAME', passwordVariable: 'SONATYPE_PASSWORD' ),
                    file(credentialsId: 'D599C1AA126762B1.asc', variable: 'PGP_PRIVATE_KEY_FILE'),
                    string(credentialsId: 'PGP_PASSPHRASE', variable: 'PGP_PASSPHRASE') ]) {
                    withMaven(maven: 'M3') {
                        sh "echo $version"
/*
                        sh(
                            script: """
                                version=$version ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
                            """
                        )
*/
                    }
                }
            }
        }
    }
}
