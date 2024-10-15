def makeWorkSpace() {
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

    // 删除当前目录中不在 items 列表中的文件和目录
    new File('.').eachFile { file ->
        if (!items.contains(file.name) || !file.isSymlink()) {
            // 如果文件或目录不在列表中或不是符号链接，则删除
            if (file.isDirectory()) {
                file.deleteDir()
            } else {
                file.delete()
            }
        }
    }

    // 对每一个项目元素创建符号链接
    items.each { item ->
        def target = new File(item)
        if (!target.exists()) {
            sh "ln -s ${workspaceDir}/${item} ${item}"
        }
    }
}

def buildCocos(String buildArg) {
    sh "${env.COCOS} --path . --build '${buildArg}'"
}

return this;