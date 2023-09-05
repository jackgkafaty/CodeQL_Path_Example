pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Docker image') {
            steps {
                sh 'docker build . --file Dockerfile --tag localbuild/testimage:latest'
            }
        }
        stage('Run Anchore Grype scan') {
            steps {
                withCredentials([string(credentialsId: 'dockerhub-credentials', variable: 'DOCKERHUB_CREDENTIALS')]) {
                    sh 'docker login -u $DOCKERHUB_CREDENTIALS_USR -p $DOCKERHUB_CREDENTIALS_PSW'
                    sh 'docker pull localbuild/testimage:latest'
                    sh 'docker run --rm -v /var/run/docker.sock:/var/run/docker.sock anchore/grype:0.9.7 --dockerfile Dockerfile localbuild/testimage:latest'
                }
            }
        }
        stage('Upload vulnerability report') {
            steps {
                withCredentials([string(credentialsId: 'github-credentials', variable: 'GITHUB_CREDENTIALS')]) {
                    sh 'curl -H "Authorization: token $GITHUB_CREDENTIALS" -H "Content-Type: application/vnd.github.v3+json" --data-binary "@${env.WORKSPACE}/grype-results.json" "https://api.github.com/repos/<owner>/<repo>/code-scanning/sarifs"'
                }
            }
        }
    }
}
