pipeline {
    agent {
        label 'jenkins-agent'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                sh "mvn clean install --batch-mode"
            }
        }

        stage('Test') {
            steps {
                echo 'Testing...'
                sh "mvn clean verify"
            }
        }

        stage('Release') {
            steps {
                echo 'Releasing artifact'
                withDockerRegistry(credentialsId: '5d633ea9-05d5-4038-bf63-a723025b95ff', url: 'https://docker.rcomanne.nl') {
                    sh 'mvn clean deploy'
                }
            }
        }

        stage('Deploy') {
            steps {
                withKubeConfig(credentialsId: 'ce42c69e-6274-4126-93f8-4339dfd0ad85', namespace: 'services') {
                    echo 'Deploying...'
                    sh "kubectl version"
                    sh "kubectl config view"
                }
            }

        }
    }
}