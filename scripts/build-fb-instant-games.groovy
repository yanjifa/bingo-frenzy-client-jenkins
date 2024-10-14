def build(String platform, Map params) {
    stage("Build ${platform}") {
        echo "Building ${platform}..."
        steps {
            def workspaceDir = "workspace-${platform}"
            dir(workspaceDir) {
                // 准备工作区
                script {
                    def script = load 'utils.groovy'
                    script.makeWorkSpace()
                }
                // 构建 Cocos 项目, 重试 3 次
                retry(3) {
                    script {
                        try {
                            def utils = load 'utils.groovy'
                            def buildArg = "platform=${platform};buildPath=./build;template=default;debug=false;md5Cache=true;"
                            utils.buildCocos(buildArg)
                        } catch (Exception e) {
                            // 如果检出失败，等待120秒后再次尝试s
                            echo "Build Cocos failed, retrying in 120 seconds. Error: ${e.getMessage()}"
                            sleep time: 120, unit: 'SECONDS'
                            throw e // 重新抛出异常以确保可以被 retry 捕获
                        }
                    }
                }
            }
        }
    }
}

return this;