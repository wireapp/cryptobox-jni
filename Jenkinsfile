pipeline {
    agent none

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
            }
        }
        stage('Upload to sonatype') {
            steps {
                sh 'echo here goes the gradle run'
            }
        }
    }
}
