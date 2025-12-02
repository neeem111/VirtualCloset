package com.example.virtualcloset.strings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.*

// Sistema de strings personalizado que responda a cambios de idioma en tiempo de ejecución
object StringProvider {

    private val englishStrings = mapOf(
        "app_name" to "VirtualCloset",
        "welcome_title" to "Virtual Closet",
        "welcome_button" to "ENTER",
        "nav_closet" to "Closet",
        "nav_dress_me" to "Dress Me",
        "nav_assistant" to "Assistant",
        "nav_profile" to "Profile",
        "closet_title" to "My Closet",
        "add_item_button" to "Add Item",
        "add_item_title" to "Add New Item",
        "add_photo" to "ADD PHOTO",
        "item_name_label" to "Item Name",
        "category_top" to "Top",
        "category_bottom" to "Bottom",
        "styles_label" to "Styles (e.g., Casual, Sporty)",
        "save_button" to "SAVE",
        "dress_me_title" to "Dress Me",
        "match_button" to "MATCH",
        "match_success" to "IT'S A MATCH!",
        "match_fail" to "MISMATCH!",
        "no_items" to "No Items!",
        "assistant_title" to "Assistant",
        "assistant_greeting" to "Hey! What do you wanna do?",
        "assistant_take_test" to "Take Style Test",
        "assistant_plan_outfits" to "Plan Outfits (Calendar)",
        "style_test_title" to "Style Test",
        "next_button" to "NEXT",
        "back_button" to "BACK",
        "test_result_title" to "As If!",
        "test_result_subtitle" to "Based on your vibes, I think this would look totally cute on you!",
        "test_result_no_outfit" to "OMG, I couldn't find anything! Add more clothes!",
        "profile_title" to "Profile & Settings",
        "profile_language" to "Language",
        "profile_font_size" to "Font Size",
        "profile_username" to "Username",
        "profile_change_assistant" to "Change Assistant",
        "calendar_title" to "Outfit Calendar",
        "plan_outfit_button" to "Plan outfit for day",
        "no_outfits_to_plan" to "Create outfits in 'Dress Me' to plan them!",
        "calendar_dialog_title" to "Outfit for"
    )

    private val spanishStrings = mapOf(
        "app_name" to "VirtualCloset",
        "welcome_title" to "Armario Virtual",
        "welcome_button" to "ENTRAR",
        "nav_closet" to "Armario",
        "nav_dress_me" to "Vístete",
        "nav_assistant" to "Asistente",
        "nav_profile" to "Perfil",
        "closet_title" to "Mi Armario",
        "add_item_button" to "Añadir Prenda",
        "add_item_title" to "Añadir Nueva Prenda",
        "add_photo" to "AÑADIR FOTO",
        "item_name_label" to "Nombre de la Prenda",
        "category_top" to "Parte Superior",
        "category_bottom" to "Parte Inferior",
        "styles_label" to "Estilos (ej. Casual, Deportivo)",
        "save_button" to "GUARDAR",
        "dress_me_title" to "Vístete",
        "match_button" to "COMBINAR",
        "match_success" to "¡ES UN MATCH!",
        "match_fail" to "¡NO COMBINA!",
        "no_items" to "¡Sin Prendas!",
        "assistant_title" to "Asistente",
        "assistant_greeting" to "¿Hola! ¿Qué quieres hacer?",
        "assistant_take_test" to "Hacer Test de Estilo",
        "assistant_plan_outfits" to "Planificar Outfits (Calendario)",
        "style_test_title" to "Test de Estilo",
        "next_button" to "SIGUIENTE",
        "back_button" to "ATRÁS",
        "test_result_title" to "¡Somos!",
        "test_result_subtitle" to "¡Basado en tus vibes, creo que este outfit te quedaría increíble!",
        "test_result_no_outfit" to "¡Ay! ¡No encontré nada! ¡Añade más prendas!",
        "profile_title" to "Perfil y Configuración",
        "profile_language" to "Idioma",
        "profile_font_size" to "Tamaño de Fuente",
        "profile_username" to "Nombre de Usuario",
        "profile_change_assistant" to "Cambiar Asistente",
        "calendar_title" to "Calendario de Outfits",
        "plan_outfit_button" to "Planificar outfit para el día",
        "no_outfits_to_plan" to "¡Crea outfits en 'Vístete' para planificarlos!",
        "calendar_dialog_title" to "Outfit para"
    )

    fun getString(key: String, language: String): String {
        val strings = when (language) {
            "es" -> spanishStrings
            else -> englishStrings
        }
        return strings[key] ?: key
    }
}

@Composable
fun rememberString(key: String, language: String): String {
    return StringProvider.getString(key, language)
}

