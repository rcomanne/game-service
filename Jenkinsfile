pipeline {
    agent {
        kubernetes {
            label 'multiple labels'
            containerTemplate {
                name 'maven'
                image 'maven:3.3.9-jdk-8-alpine'
                command 'sleep'
                args '9999999'
            }
            podRetention onFailure()
        }
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