pipeline {
    agent any
    stages {
        stage('Print Parameters') {
            steps {
                script {
                    echo "All Parameters:"
                    params.each { key, value ->
                        echo "${key}: ${value}"
                    }
                }
            }
        }
        stage('Check Out Code') {
            steps {
                script {
                    echo "Start check out code..."
                    retry(3) {
                        try {
                            dir('bingo-frenzy-client') {
                                checkout scm: [
                                    $class: 'GitSCM',
                                    branches: [[name: params.TARGET_COMMIT_ID]],
                                    userRemoteConfigs: [[url: 'git@github.com:joycastle/bingo-frenzy-client.git']],
                                    extensions: [
                                        [$class: 'CloneOption', timeout: 120, reference: '/Users/jenkins/.jenkins/workspace/bingo_build_apk']
                                    ]
                                ]
                            }
                        } catch (Exception e) {
                            // 如果检出失败，等待120秒后再次尝试
                            echo "Checkout failed, retrying in 120 seconds. Error: ${e.getMessage()}"
                            sleep time: 120, unit: 'SECONDS'
                            throw e // 重新抛出异常以确保可以被 retry 捕获
                        }
                    }
                }
            }
        }
        stage('Compress Texture') {
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
                        def currentPlatform = platform // 创建新的局部变量以避免闭包问题
                        buildStages["Build ${currentPlatform}"] = {
                            def script = load "scripts/build-${currentPlatform}.groovy"
                            script.build(currentPlatform, params)
                        }
                    }
                    // 执行并行阶段
                    parallel buildStages
                }
            }
        }
    }
}