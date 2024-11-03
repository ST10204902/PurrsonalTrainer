package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.util.Log
import android.util.TypedValue
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager

// Add Cat Avatars Here
private val CatAvatarDictionary: Map<Int, List<Int>> = mapOf(
    1 to listOf(
        R.drawable.cat_stage1,
        R.drawable.cat_stage1_black
    ),
    2 to listOf(
        R.drawable.cat_stage2,
        R.drawable.cat_stage2_black
    ),
    3 to listOf(
        R.drawable.cat_stage3,
        R.drawable.cat_stage3_black
    ),
    4 to listOf(
        R.drawable.cat_stage4,
        R.drawable.cat_stage4_black
    ),
    5 to listOf(
        R.drawable.cat_stage5,
        R.drawable.cat_stage5_black
    ),
    6 to listOf(
        R.drawable.cat_stage6,
        R.drawable.cat_stage6_black
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
        level < 60 -> 6
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

