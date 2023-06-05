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
                sh "make dist"
                archiveArtifacts artifacts: 'dist/*', followSymlinks: false
            }
        }
        stage('Upload to sonatype') {
            steps {
                sh 'echo here goes the gradle run'
            }
        }
    }
}
