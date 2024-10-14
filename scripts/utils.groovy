def makeWorkSpace() {
    script {
        // 定义所有需要链接的文件或目录
        def items = [
            '@types',
            'assets',
            'build-templates',
            'local',
            'node_modules',
            'packages',
            'package.json',
            'settings',
            'project.json',
            'template-banner.png',
            'template.json',
            'tsconfig.cocos.json',
            'tsconfig.json',
            'jsconfig.json'
         ]
        // 使用 Jenkins 的 WORKSPACE 环境变量
        def workspaceDir = "${env.WORKSPACE}/bingo-frenzy-client"
        // 对每一个项目元素创建符号链接
        items.each { item ->
            sh "ln -s ${workspaceDir}/${item} ${item}"
        }
    }
}

def buildCocos(buildArg String) {
    sh "${env.COCOS} --path . --build '${buildArg}'"
}

return this;