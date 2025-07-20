package com.luminarixlabs.tempest.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "TempestSettings",
    storages = [Storage("tempest.xml")]
)
@Service(Service.Level.APP)
class TempestSettings : PersistentStateComponent<TempestSettings> {
    
    var isEnabled: Boolean = true
    
    override fun getState(): TempestSettings = this
    
    override fun loadState(state: TempestSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    companion object {
        fun getInstance(): TempestSettings {
            return ApplicationManager.getApplication().getService(TempestSettings::class.java)
        }
    }
}