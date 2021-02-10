pipeline {
    agent {
        label 'jenkins-agent'
    }

    tools {
        maven 'mvn'
    }

    stages {
        stage('Build') {
            steps {
                sh "whoami"
                sh "id"
                echo 'Building...'
                sh "mvn clean install"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing...'
                sh "mvn clean verify"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
                sh "kubectl version"
                sh "kubectl config view"
            }

        }
    }
}