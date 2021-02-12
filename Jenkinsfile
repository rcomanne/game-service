pipeline {
    agent none

    triggers {
        githubPush()
    }

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

        stage('Release') {
            agent {
                label 'jenkins-agent'
            }
            steps {
                echo 'Releasing artifact'
                withDockerRegistry(credentialsId: '5d633ea9-05d5-4038-bf63-a723025b95ff', url: 'https://docker.rcomanne.nl') {
                    sh 'mvn clean deploy'
                }
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