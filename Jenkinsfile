pipeline {
    agent any

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