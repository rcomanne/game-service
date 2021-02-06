pipeline {
    agent {
        kubernetes {
            label 'service-maven'
            image 'maven:3-adoptopenjdk-11'
            command: 'sleep'
            args: '999999'
        }
        podRetention onFailure()
    }

    stages {
        stage('Build') {
            steps {
                container('maven') {
                    echo 'Building...'
                    sh "mvn clean install"
                }
            }
        }
        stage('Test') {
            steps {
                container('maven') {
                    echo 'Testing...'
                    sh "mvn clean verify"
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
                sh "kubectl version"
            }
        }
    }
}