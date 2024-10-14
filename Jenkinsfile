import java.nio.file.Files
import java.nio.file.Paths
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

pipeline {
    agent any
    environment {
        COCOS = '/Applications/Cocos/Creator/2.4.13/CocosCreator.app/Contents/MacOS/CocosCreator'
        PROJECT_PATH = 'bingo-frenzy-client'
    }
    stages {
        // 打印参数
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
        // 检出代码
        stage('Check Out Code') {
            steps {
                dir("${env.PROJECT_PATH}") {
                    // 检出代码, 重试 3 次
                    script {
                        echo "Start check out code..."
                        retry(3) {
                            try {
                                checkout scm: [
                                    $class: 'GitSCM',
                                    branches: [[name: params.TARGET_COMMIT_ID]],
                                    userRemoteConfigs: [[url: 'git@github.com:joycastle/bingo-frenzy-client.git']],
                                    extensions: [
                                        [$class: 'CloneOption', timeout: 120],
                                        [$class: 'CleanCheckout']                                    ]
                                ]
                            } catch (Exception e) {
                                // 如果检出失败，等待120秒后再次尝试
                                echo "Checkout failed, retrying in 120 seconds. Error: ${e.getMessage()}"
                                sleep time: 120, unit: 'SECONDS'
                                throw e // 重新抛出异常以确保可以被 retry 捕获
                            }
                        }
                    }
                    // 更新子模块
                    script {
                        echo "Update Submodule..."
                        script {
                            def submodules = [
                                "engine",
                                "build/jsb-default/frameworks/cocos2d-x"
                                // "engine-native"
                            ]
                            submodules.each {
                                sh "git submodule update --init --recursive ${it}"
                            }
                        }
                    }
                }
            }
        }
        // 安装依赖
        stage ('Dependencies') {
            steps {
                // 自定义引擎
                script {
                    echo "Customizing engine..."
                    def jsonFilePath = "${env.WORKSPACE}/${env.PROJECT_PATH}/local/settings_template.json"
                    def settingText = new String(Files.readAllBytes(Paths.get(jsonFilePath)))
                    def slurper = new JsonSlurper()
                    def data = slurper.parseText(settingText)
                    data['js-engine-pat'] = "${env.WORKSPACE}/${env.PROJECT_PATH}/engine";
                    data['cpp-engine-path'] = "${env.WORKSPACE}/${env.PROJECT_PATH}/build/jsb-default/frameworks/cocos2d-x";
                    def updatedJsonText = JsonOutput.toJson(data)
                    def prettyJsonText = JsonOutput.prettyPrint(updatedJsonText)
                    Files.write(path, prettyJsonText.getBytes("UTF-8"))
                }
                // 安装依赖
                dir("${env.PROJECT_PATH}") {
                    echo "Installing dependencies..."
                    sh 'yarn'
                }
            }
        }
        // 压缩纹理
        stage('Compress Texture') {
            steps {
                echo "Compressing texture..."
            }
        }
        // 下载配置文件
        stage('Download Configs') {
            steps {
                // 下载配置文件, 后面 Link Bundles 阶段会用到
                echo "Downloading configs..."
            }
        }
        // 链接 Bundle
        stage('Link Bundles') {
            steps {
                // 根据配置决定哪些 Bundle 需要链接
                echo "Linking bundles..."
            }
        }
        // 动态并行构建
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