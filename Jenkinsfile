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
                sh "cd android && make dist"
                archiveArtifacts artifacts: 'android/dist/*', followSymlinks: false
            }
        }
        stage('Upload to sonatype') {
            steps {
                sh 'echo here goes the gradle run'
            }
        }
    }
}
