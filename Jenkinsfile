pipeline {
    agent any
    stages {
        stage('print params') {
            steps {
                script {
                    echo "All Parameters:"
                    params.each { key, value ->
                        echo "${key}: ${value}"
                    }
                }
            }
        }
        stage('checkout code') {
            steps {
                script {
                    echo "Start check out code..."
                    retry(3) { // 如果检出失败，尝试最多三次
                        try {
                            dir('bingo-frenzy-client') {
                                checkout scm: [
                                    $class: 'GitSCM',
                                    branches: [[name: params.BRANCH_NAME]],
                                    userRemoteConfigs: [[url: 'git@github.com:joycastle/bingo-frenzy-client.git']],
                                    extensions: [
                                        [$class: 'CloneOption', timeout: 120]
                                    ]
                                ]
                            }
                        } catch (e) {
                            // 如果检出失败，等待120秒后再次尝试
                            echo "Checkout failed, retrying in 120 seconds..."
                            sleep time: 120, unit: 'SECONDS'
                            throw e // 重新抛出异常以确保可以被 retry 捕获
                        }
                    }
                }
            }
        }
        stage('compress texture') {
            steps {
                echo "Compressing texture..."
            }
        }
        stage('Dynamic Parallel Build') {
            steps {
                script {
                    def platforms = params.BUILD_PLATFORMS.tokenize(',')
                    def buildStages = [:]

                    // 遍历每个平台并添加到并行构建阶段
                    for (platform in platforms) {
                        buildStages["Build ${platform.capitalize()}"] = {
                            def script = load "scripts/build-${platform}.groovy"
                            script.build(platform, params)
                        }
                    }
                    // 执行并行阶段
                    parallel buildStages
                }
            }
        }
    }
}