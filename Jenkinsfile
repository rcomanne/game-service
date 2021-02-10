pipeline {
    agent {
        label 'jenkins-slave'
    }

    tools {
        maven 'mvn'
        dockerTool 'docker'
    }

    stages {
        stage('Build') {
            steps {
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