package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.util.Log
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager

// Add Cat Avatars Here
private val CatAvatarDictionary: Map<Int, List<Int>> = mapOf(
    1 to listOf(
        R.drawable.cat_stage1,
        R.drawable.cat_stage1_black,
        R.drawable.cat_stage1_greystripey,
        R.drawable.cat_stage1_white,
        R.drawable.cat_stage1_tabby,
        R.drawable.cat_stage1_michael,
        R.drawable.cat_stage1_tuxedo,
        ),
    2 to listOf(
        R.drawable.cat_stage2,
        R.drawable.cat_stage2_black,
        R.drawable.cat_stage2_greystripey,
        R.drawable.cat_stage2_white,
        R.drawable.cat_stage2_tabby,
        R.drawable.cat_stage2_michael,
        R.drawable.cat_stage2_tuxedo,
        ),
    3 to listOf(
        R.drawable.cat_stage3,
        R.drawable.cat_stage3_black,
        R.drawable.cat_stage3_greystrippey,
        R.drawable.cat_stage3_white,
        R.drawable.cat_stage3_tabby,
        R.drawable.cat_stage3_michael,
        R.drawable.cat_stage3_tuxedo,
        ),
    4 to listOf(
        R.drawable.cat_stage4,
        R.drawable.cat_stage4_black,
        R.drawable.cat_stage4_greystrippey,
        R.drawable.cat_stage4_white,
        R.drawable.cat_stage4_tabby,
        R.drawable.cat_stage4_michael,
        R.drawable.cat_stage4_tuxedo,
        ),
    5 to listOf(
        R.drawable.cat_stage5,
        R.drawable.cat_stage5_black,
        R.drawable.cat_state5_greystrippey,
        R.drawable.cat_state5_white,
        R.drawable.cat_statge5_tabby,
        R.drawable.cat_state5_michael,
        R.drawable.cat_state5_tuxedo,
        ),
    6 to listOf(
        R.drawable.cat_stage6,
        R.drawable.cat_stage6_black,
        R.drawable.cat_state6_greystrippey,
        R.drawable.cat_state6_white,
        R.drawable.cat_stage6_tabby,
        R.drawable.cat_state6_michael,
        R.drawable.cat_state6_tuxedo,
        )
)

fun GetCatStage(): Int
{
    var level = 0
    try
    {
        level = UserManager.user!!.level
    }
    catch (e: Exception)
    {
        Log.e("CatService", "Failed to get User level", e)
    }

    return when {
        level < 10 -> 1
        level < 20 -> 2
        level < 30 -> 3
        level < 40 -> 4
        level < 50 -> 5
        level >= 60 -> 6
        else -> 1
    }
}

fun GetCatDrawableId(): Int
{
    try
    {
        val position = UserManager.user!!.catURI.toInt()
        val catStage = GetCatStage()
        val avatarGroup = CatAvatarDictionary[catStage]
        if (avatarGroup != null)
        {
            return avatarGroup[position]
        }
    }
    catch (e: Exception)
    {
        Log.e("CatService", "Failed to get Cat Drawable Id", e)
    }

    return R.drawable.cat_placeholder
}

fun GetCatAvatars(): List<Int>
{
    try
    {
        val catStage = GetCatStage()
        val avatarGroup = CatAvatarDictionary[catStage]
        if (avatarGroup != null)
        {
            return avatarGroup
        }
    }
    catch (e: Exception)
    {
        Log.e("CatService", "Failed to get Cat Avatars List", e)
    }
    return listOf(R.drawable.cat_placeholder)
}

