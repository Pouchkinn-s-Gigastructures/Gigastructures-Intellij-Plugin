package com.github.ttftcuts.gigastructuresintellijplugin.startup

import com.github.ttftcuts.gigastructuresintellijplugin.main.data.ToolData
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class GigaToolsInit : ProjectActivity {

    override suspend fun execute(project: Project) {
        ToolData.loadDataFile(project)
    }
}