pipeline {
    agent {
        kubernetes
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
            }
        }
    }
}