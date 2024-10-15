def build(String platform, Map params) {
    // 加载工具类
    def utils = load 'scripts/utils.groovy'
    dir("workspace/${platform}") {
        // 准备工作区
        utils.makeWorkSpace()
        // 延迟
        sleep time: 10, unit: 'SECONDS'
        // 构建 Cocos 项目, 重试 3 次
        retry(3) {
            try {
                def buildArg = "platform=${platform};buildPath=./build;template=default;debug=false;"
                utils.buildCocos(buildArg)
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    // 中断异常，可能手动取消, 不重试
                    echo 'Build Cocos was aborted by user.'
                } else {
                    // 如果构建失败，等待 30 秒后再次尝试
                    echo "Build Cocos failed, retrying in 30 seconds. Error: ${e.getMessage()}"
                    sleep time: 30, unit: 'SECONDS'
                    throw e // 重新抛出异常以确保可以被 retry 捕获
                }
            }
        }
    }
}

def buildNative(String platform, Map params) {
    dir("workspace/${platform}") {
    }
}

return this;