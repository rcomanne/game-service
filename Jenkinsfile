pipeline {
    agent none

    tools {
        maven 'mvn'
    }

    stages {
        stage('Build') {
            agent {
                label 'jenkins-agent'
            }
            steps {
                echo 'Building...'
                sh "mvn clean install --batch-mode"
            }
        }

        stage('Test') {
            agent {
                label 'jenkins-agent'
            }
            steps {
                echo 'Testing...'
                sh "mvn clean verify"
            }
        }

        stage('Deploy') {
            agent {
                label 'kubectl'
            }
            steps {
                echo 'Deploying...'
                sh "kubectl version"
                sh "kubectl config view"
            }

        }
    }
}