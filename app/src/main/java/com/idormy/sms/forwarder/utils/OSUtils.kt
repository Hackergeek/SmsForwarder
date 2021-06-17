package com.idormy.sms.forwarder.utils

import android.text.TextUtils
import java.io.IOException

/**
 * 使用方法:
 * OSUtils.ROM_TYPE romType = OSUtils.getRomType();
 * 可能您需要对其他的ROM进行区分，那么只需三步：
 * 一：使用BuildProperties获取到所有的key,遍历获取到所有的value(getProperty),或者直接找到build.prop文件。
 * 二：找到定制ROM特征的标识（key/value）
 * 三：增加ROM_TYPE枚举类型，getRomType方法加入识别比对即可
 * 作者：YouAreMyShine
 * 链接：https://www.jianshu.com/p/bb1f765a425f
 */
object OSUtils {
    /**
     * MIUI ROM标识
     *
     *
     * "ro.miui.ui.version.code" -> "5"
     *
     *
     * "ro.miui.ui.version.name" -> "V7"
     *
     *
     * "ro.miui.has_handy_mode_sf" -> "1"
     *
     *
     * "ro.miui.has_real_blur" -> "1"
     *
     *
     *
     *
     *
     *
     * Flyme ROM标识
     *
     *
     * "ro.build.user" -> "flyme"
     *
     *
     * "persist.sys.use.flyme.icon" -> "true"
     *
     *
     * "ro.flyme.published" -> "true"
     *
     *
     * "ro.build.display.id" -> "Flyme OS 5.1.2.0U"
     *
     *
     * "ro.meizu.setupwizard.flyme" -> "true"
     *
     *
     *
     *
     *
     *
     * EMUI ROM标识
     *
     *
     * "ro.build.version.emui" -> "EmotionUI_1.6"
     */
    //MIUI标识
    private const val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
    private const val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"

    //EMUI标识
    private const val KEY_EMUI_VERSION_CODE = "ro.build.version.emui"

    //Flyme标识
    private const val KEY_FLYME_ID_FALG_KEY = "ro.build.display.id"
    private const val KEY_FLYME_ID_FALG_VALUE_KEYWORD = "Flyme"
    private const val KEY_FLYME_ICON_FALG = "persist.sys.use.flyme.icon"
    private const val KEY_FLYME_SETUP_FALG = "ro.meizu.setupwizard.flyme"
    private const val KEY_FLYME_PUBLISH_FALG = "ro.flyme.published"

    /**
     * @param
     * @return ROM_TYPE ROM类型的枚举
     * @datecreate at 2016/5/11 0011 9:46
     * @mehtodgetRomType
     * @description获取ROM类型，MIUI_ROM, *FLYME_ROM,    * EMUI_ROM,    * OTHER_ROM
     */
    val romType: RomType
        get() {
            val localRomType = RomType.OTHER_ROM
            try {
                val buildProperties = BuildProperties.instance
                if (buildProperties!!.containsKey(KEY_EMUI_VERSION_CODE)) {
                    return RomType.EMUI_ROM
                }
                if (buildProperties.containsKey(KEY_MIUI_VERSION_CODE) || buildProperties.containsKey(
                        KEY_MIUI_VERSION_NAME
                    )
                ) {
                    return RomType.MIUI_ROM
                }
                if (buildProperties.containsKey(KEY_FLYME_ICON_FALG) || buildProperties.containsKey(
                        KEY_FLYME_SETUP_FALG
                    ) || buildProperties.containsKey(KEY_FLYME_PUBLISH_FALG)
                ) {
                    return RomType.FLYME_ROM
                }
                if (buildProperties.containsKey(KEY_FLYME_ID_FALG_KEY)) {
                    val romName = buildProperties.getProperty(KEY_FLYME_ID_FALG_KEY)
                    if (!TextUtils.isEmpty(romName) && romName.contains(
                            KEY_FLYME_ID_FALG_VALUE_KEYWORD
                        )
                    ) {
                        return RomType.FLYME_ROM
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return localRomType
        }

    enum class RomType {
        MIUI_ROM, FLYME_ROM, EMUI_ROM, OTHER_ROM
    }
}