pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                mvn clean install
            }
        }
        stage('Test') {
            steps {
                echo 'Testing...'
                mvn clean verify
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